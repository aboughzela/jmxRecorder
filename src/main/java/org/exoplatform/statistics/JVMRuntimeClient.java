package org.exoplatform.statistics;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.*;


public class JVMRuntimeClient {

    private static CsvFileGenerator fileGenerator;

    private static List<String []> configs;


    public static void main(String[] args) throws Exception
    {
        CsvFileGenerator fileConfigs =  new CsvFileGenerator("cache.csv");
        configs = fileConfigs.readFile();

        fileGenerator =  new CsvFileGenerator("record-"+System.currentTimeMillis()+".csv") ;
        if (args == null)
        {
            System.out.println("Usage: java JVMRuntimeClient HOST-1:PORT-1 HOST-2:PORT-2");
        }
        if(args.length < 2)
        {
            System.out.println("Usage: java JVMRuntimeClient HOST-1:PORT-1 HOST-2:PORT-2");
        }

        try
        {
            /**
             * Init jmx services
             */
            JMXServiceURL url1 = new JMXServiceURL(
                    "service:jmx:rmi:///jndi/rmi://"+args[0]+"/jmxrmi");
            JMXConnector connector1 = JMXConnectorFactory.connect(url1, null);

            MBeanServerConnection remote1 = connector1.getMBeanServerConnection();

            JMXServiceURL url2 = new JMXServiceURL(
                    "service:jmx:rmi:///jndi/rmi://"+args[1]+"/jmxrmi");
            JMXConnector connector2 = JMXConnectorFactory.connect(url2, null);

            MBeanServerConnection remote2 = connector2.getMBeanServerConnection();

            MBeanServerConnection remote3 = null;
            JMXConnector connector3 = null;

            if(args.length == 3){
                JMXServiceURL url3 = new JMXServiceURL(
                        "service:jmx:rmi:///jndi/rmi://"+args[2]+"/jmxrmi");
                connector3 = JMXConnectorFactory.connect(url3, null);
                remote3 = connector3.getMBeanServerConnection();
            }

            Collection<Object> collection = new ArrayList<Object>();

            fileGenerator.writeLine(collection);
            fileGenerator.writeLine(null);
            collection.clear();

            //NODE 1
            for (Object o : MbeanCache.exoParams){
                collection.add(o);
            }
            for (Object o : MbeanCache.ispnParams){
                collection.add(o);
            }

            //NODE 2
            for (Object o : MbeanCache.exoParams){
                collection.add(o);
            }
            for (Object o : MbeanCache.ispnParams){
                collection.add(o);
            }

            //NODE 3
            if(remote3 != null)
            {
                for (Object o : MbeanCache.exoParams){
                    collection.add(o);
                }
                for (Object o : MbeanCache.ispnParams){
                    collection.add(o);
                }
            }
            fileGenerator.writeLine(collection);
            fileGenerator.writeLine(null);



            for(int i = 0 ;  i< MbeanCache.services.length; i++){

                fileGenerator.writeLine(null);

                collection.add(MbeanCache.services[i] + " CACHE");
                fileGenerator.writeLine(collection);
                fileGenerator.writeLine(null);

                for(String[] data : configs){

                    if(MbeanCache.services[i].equals(data[1])){
                        recodeCacheData(remote1, remote2, remote3, data[0], data[2]);
                    }
                }

            }


            //JGroups Statistics
            fileGenerator.writeLine(null);
            collection.add("JGroups Statistics");
            fileGenerator.writeLine(collection);
            fileGenerator.writeLine(null);

            collection.add("node cluster : 1");
            fileGenerator.writeLine(collection);
            fileGenerator.writeLine(null);

            for (Object o : MbeanCache.jgroupsParams){
                collection.add(o);
            }
            fileGenerator.writeLine(collection);

            ObjectName bean1 = new ObjectName(MbeanCache.jgroups);
            for (String attr : MbeanCache.jgroupsParams)
            {
                collection.add(remote1.getAttribute(bean1,attr));
            }

            fileGenerator.writeLine(collection);


            fileGenerator.writeLine(null);
            collection.add("node cluster : 2");
            fileGenerator.writeLine(collection);
            for (Object o : MbeanCache.jgroupsParams){
                collection.add(o);
            }
            fileGenerator.writeLine(collection);

            ObjectName bean2 = new ObjectName(MbeanCache.jgroups);
            for (String attr : MbeanCache.jgroupsParams)
            {
                collection.add(remote2.getAttribute(bean2,attr));
            }
            fileGenerator.writeLine(collection);


            if(remote3 != null)
            {
                fileGenerator.writeLine(null);
                collection.add("node cluster : 3");
                fileGenerator.writeLine(collection);
                for (Object o : MbeanCache.jgroupsParams){
                    collection.add(o);
                }
                fileGenerator.writeLine(collection);

                ObjectName bean3 = new ObjectName(MbeanCache.jgroups);
                for (String attr : MbeanCache.jgroupsParams)
                {
                    collection.add(remote3.getAttribute(bean3,attr));
                }
                fileGenerator.writeLine(collection);
            }





            //save stream
            fileGenerator.flush();



            connector1.close();
            connector2.close();

            if(connector3 != null){
                connector3.close();
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
            System.exit(0);
        }
    }

    private static void recodeCacheData(MBeanServerConnection remote1, MBeanServerConnection remote2,
                                        MBeanServerConnection remote3, String name, String mode) throws Exception {


        ArrayList<Object> result= new ArrayList<Object>();


        recordRemote(name, result, remote1, mode);

        recordRemote(name, result, remote2, mode);


        //NODE 3
        if(remote3 != null)
        {
            recordRemote(name, result, remote3, mode);
        }

        fileGenerator.writeLine(result);

    }


    private static void recordRemote(String name, ArrayList<Object> result, MBeanServerConnection remote, String mode ) throws Exception{
        if(name.contains(".")) {
            getMBeanAttributes(MbeanCache.getExoCache(name, MbeanCache.exoName1),
                    result, MbeanCache.exoParams, remote);

        } else {
            getMBeanAttributes(MbeanCache.getExoCache(name, MbeanCache.exoName),
                    result, MbeanCache.exoParams, remote);
        }


        if(MbeanCache.ASYNCREPLICATION.equals(mode) || MbeanCache.asyncInvalidation.equals(mode)){
            getMBeanAttributes(MbeanCache.getExoCache(name, MbeanCache.ispnNameAsync),
                    result, MbeanCache.ispnParams, remote);
        } else {
            getMBeanAttributes(MbeanCache.getExoCache(name, MbeanCache.ispnName),
                    result, MbeanCache.ispnParams, remote);
        }
    }
    private static ArrayList<Object> getMBeanAttributes(String mbeanName,  ArrayList<Object> values, List<String> params
    , MBeanServerConnection remote) throws Exception {
        ObjectName bean = new ObjectName(mbeanName);
        for (String attr : params)
        {
            try{
                values.add(remote.getAttribute(bean,attr));
            }catch (Exception e) {
                break;
            }
        }

        return values;
    }


}
