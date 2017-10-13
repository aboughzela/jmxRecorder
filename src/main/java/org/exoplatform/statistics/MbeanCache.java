package org.exoplatform.statistics;


import java.util.Arrays;
import java.util.List;

public class MbeanCache {

    public static final String REPLICATION = "replication";

    public static final String ASYNCREPLICATION = "asyncReplication";

    public static final String asyncInvalidation = "asyncInvalidation";

    public static final String SYNCINVALIDATION = "syncInvalidation";

    public static final String[] services = {"social","portal","common","wiki","forum","calendar"};


    public static final String jgroups ="services.ispn.cache:type=channel,cluster=\"DefaultPartition-services-portal\"";

    public static final String exoName ="exo:portal=portal,service=cache,name={Name}";
    public static final String exoName1 ="exo:portal=portal,service=cache,name=\"{Name}\"";

    public static final String ispnName =
            "services.ispn.cache:type=Cache,name=\"{Name}(repl_sync)\",manager=\"template_portal\",component=RpcManager";

    public static final String ispnNameAsync =
            "services.ispn.async.cache:type=Cache,name=\"{Name}(repl_async)\",manager=\"template_portal_{Name}_portal\",component=RpcManager";


    public static final List<String> exoParams = Arrays.asList("Name", "MaxEntries", "Size", "HitCount", "MissCount");

    public static final List<String> ispnParams = Arrays.asList("replicationCount", "replicationFailures");

    public static final List<String> jgroupsParams = Arrays.asList("received_bytes", "received_messages","sent_bytes","sent_messages");


    public static String getExoCache(String cacheName, String pattern){

        return pattern.replace("{Name}", cacheName);
    }



}
