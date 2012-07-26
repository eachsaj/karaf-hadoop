/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.karaf;

import org.apache.hadoop.karaf.hdfs.DataNodeFactory;
import org.apache.hadoop.karaf.hdfs.NameNodeFactory;
import org.apache.hadoop.karaf.hdfs.SecondaryNameNodeFactory;
import org.apache.hadoop.karaf.mapred.JobTrackerFactory;
import org.apache.hadoop.karaf.mapred.TaskTrackerFactory;
import org.apache.hadoop.metrics2.lib.DefaultMetricsSystem;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import javax.security.auth.Subject;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Dictionary;

public class HadoopFactory implements ManagedService {

    public static final String CONFIG_PID = "org.apache.hadoop";

    private DataNodeFactory dataNodeFactory;
    private NameNodeFactory nameNodeFactory;
    private SecondaryNameNodeFactory secondaryNameNodeFactory;
    private JobTrackerFactory jobTrackerFactory;
    private TaskTrackerFactory taskTrackerFactory;

    public DataNodeFactory getDataNodeFactory() {
        return dataNodeFactory;
    }

    public void setDataNodeFactory(DataNodeFactory dataNodeFactory) {
        this.dataNodeFactory = dataNodeFactory;
    }

    public NameNodeFactory getNameNodeFactory() {
        return nameNodeFactory;
    }

    public void setNameNodeFactory(NameNodeFactory nameNodeFactory) {
        this.nameNodeFactory = nameNodeFactory;
    }

    public SecondaryNameNodeFactory getSecondaryNameNodeFactory() {
        return secondaryNameNodeFactory;
    }

    public void setSecondaryNameNodeFactory(SecondaryNameNodeFactory secondaryNameNodeFactory) {
        this.secondaryNameNodeFactory = secondaryNameNodeFactory;
    }

    public JobTrackerFactory getJobTrackerFactory() {
        return jobTrackerFactory;
    }

    public void setJobTrackerFactory(JobTrackerFactory jobTrackerFactory) {
        this.jobTrackerFactory = jobTrackerFactory;
    }

    public TaskTrackerFactory getTaskTrackerFactory() {
        return taskTrackerFactory;
    }

    public void setTaskTrackerFactory(TaskTrackerFactory taskTrackerFactory) {
        this.taskTrackerFactory = taskTrackerFactory;
    }

    public void updated(Dictionary properties) throws ConfigurationException {
        updateFactory(properties, "nameNode", nameNodeFactory);
        updateFactory(properties, "dataNode", dataNodeFactory);
        updateFactory(properties, "secondaryNameNode", secondaryNameNodeFactory);
        updateFactory(properties, "jobTracker", jobTrackerFactory);
        updateFactory(properties, "taskTracker", taskTrackerFactory);
    }

    private void updateFactory(final Dictionary properties, final String prop, final Factory<?> factory) throws ConfigurationException {
        if (getBoolean(properties, prop)) {
            try {
                Subject.doAs(null, new PrivilegedExceptionAction<Object>() {
                    @Override
                    public Object run() throws Exception {
                        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                        try {
                            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                            factory.delete();
                            factory.create(properties);
                        } finally {
                            Thread.currentThread().setContextClassLoader(tccl);
                        }
                        return null;
                    }
                });
            } catch (PrivilegedActionException e) {
                if (e.getCause() instanceof ConfigurationException) {
                    throw (ConfigurationException) e.getCause();
                } else {
                    throw new RuntimeException(e.getCause());
                }
            }
        } else {
            factory.delete();
        }
    }

    public void destroy() throws ConfigurationException {
        updated(null);
        DefaultMetricsSystem.INSTANCE.shutdown();
    }

    private boolean getBoolean(Dictionary properties, String key) {
        Object value = properties != null ? properties.get(key) : null;
        if (value != null) {
            return Boolean.parseBoolean(value.toString());
        } else {
            return false;
        }
    }

}
