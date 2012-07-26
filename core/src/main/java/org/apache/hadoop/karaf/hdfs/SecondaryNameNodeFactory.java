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
package org.apache.hadoop.karaf.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode;
import org.apache.hadoop.karaf.Factory;
import org.apache.hadoop.util.Daemon;

import java.util.Dictionary;
import java.util.Enumeration;

public class SecondaryNameNodeFactory extends Factory<SecondaryNameNode> {

    @Override
    protected SecondaryNameNode doCreate(Dictionary properties) throws Exception {
        Configuration conf = new Configuration();
        for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
            Object key = e.nextElement();
            Object value = properties.get(key);
            conf.set(key.toString(), value.toString());
        }
        SecondaryNameNode secondaryNameNode = new SecondaryNameNode();
        new Daemon(secondaryNameNode).start();
        return secondaryNameNode;
    }

    @Override
    protected void doDelete(SecondaryNameNode service) throws Exception {
        service.shutdown();
    }

}
