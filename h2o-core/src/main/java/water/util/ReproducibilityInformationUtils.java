package water.util;

import org.joda.time.DateTimeZone;
import water.*;
import water.api.RestApiExtension;
import water.parser.ParseTime;

import java.util.*;

public class ReproducibilityInformationUtils {
  public static TwoDimTable createNodeInformationTable() {
    List<String> colHeaders = new ArrayList<>();
    List<String> colTypes = new ArrayList<>();
    List<String> colFormat = new ArrayList<>();

    colHeaders.add("node"); colTypes.add("int"); colFormat.add("%d");
    colHeaders.add("h2o"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("healthy"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("last_ping"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("num_cpus"); colTypes.add("int"); colFormat.add("%d");
    colHeaders.add("sys_load"); colTypes.add("double"); colFormat.add("%.5f");
    colHeaders.add("mem_value_size"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("free_mem"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("pojo_mem"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("swap_mem"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("free_disc"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("max_disc"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("pid"); colTypes.add("int"); colFormat.add("%d");
    colHeaders.add("num_keys"); colTypes.add("int"); colFormat.add("%d");
    colHeaders.add("tcps_active"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("open_fds"); colTypes.add("int"); colFormat.add("%d");
    colHeaders.add("rpcs_active"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("nthreads"); colTypes.add("int"); colFormat.add("%d");
    colHeaders.add("is_leader"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("total_mem"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("max_mem"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("java_version"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("jvm_launch_parameters"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("jvm_pid"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("os_version"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("machine_physical_mem"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("machine_locale"); colTypes.add("string"); colFormat.add("%s");

    H2ONode[] members = H2O.CLOUD.members();

    final int rows = members.length;
    TwoDimTable table = new TwoDimTable(
            "Node Information", null,
            new String[rows],
            colHeaders.toArray(new String[0]),
            colTypes.toArray(new String[0]),
            colFormat.toArray(new String[0]),
            "");


    for (int row = 0; row < rows; row++) {
      IcedHashMap<String, Object> nodeConfiguration = members[row].getConfiguration();
      int col = 0;
      table.set(row, col++, row + 1);
      table.set(row, col++, members[row].getIpPortString());
      table.set(row, col++, Boolean.toString(members[row].isHealthy()));
      table.set(row, col++, members[row]._last_heard_from);
      table.set(row, col++, (int) members[row]._heartbeat._num_cpus);
      table.set(row, col++, members[row]._heartbeat._system_load_average);
      table.set(row, col++, members[row]._heartbeat.get_kv_mem());
      table.set(row, col++, members[row]._heartbeat.get_free_mem());
      table.set(row, col++, members[row]._heartbeat.get_pojo_mem());
      table.set(row, col++, members[row]._heartbeat.get_swap_mem());
      table.set(row, col++, members[row]._heartbeat.get_free_disk());
      table.set(row, col++, members[row]._heartbeat.get_max_disk());
      table.set(row, col++, members[row]._heartbeat._pid);
      table.set(row, col++, members[row]._heartbeat._keys);
      table.set(row, col++, members[row]._heartbeat._tcps_active);
      table.set(row, col++, members[row]._heartbeat._process_num_open_fds);
      table.set(row, col++, members[row]._heartbeat._rpcs);
      table.set(row, col++, (int) members[row]._heartbeat._nthreads);
      table.set(row, col++, Boolean.toString(row == H2O.CLOUD.leader().index() ? true : false));
      table.set(row, col++, nodeConfiguration.get("_total_mem"));
      table.set(row, col++, nodeConfiguration.get("_max_mem"));
      table.set(row, col++, nodeConfiguration.get("_java_version"));
      table.set(row, col++, nodeConfiguration.get("_jvm_launch_parameters"));
      table.set(row, col++, nodeConfiguration.get("_jvm_pid"));
      table.set(row, col++, nodeConfiguration.get("_os_version"));
      table.set(row, col++, nodeConfiguration.get("_machine_psysical_mem"));
      table.set(row, col++, nodeConfiguration.get("_machine_locale"));
    }
    return table;
  }

  public static TwoDimTable createClusterConfigurationTable() {
    List<String> colHeaders = new ArrayList<>();
    List<String> colTypes = new ArrayList<>();
    List<String> colFormat = new ArrayList<>();

    colHeaders.add("H2O cluster uptime"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("H2O cluster timezone"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("H2O data parsing timezone"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("H2O cluster version"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("H2O cluster version age"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("H2O cluster name"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("H2O cluster total nodes"); colTypes.add("int"); colFormat.add("%d");
    colHeaders.add("H2O cluster free memory"); colTypes.add("long"); colFormat.add("%d");
    colHeaders.add("H2O cluster total cores"); colTypes.add("int"); colFormat.add("%d");
    colHeaders.add("H2O cluster allowed cores"); colTypes.add("int"); colFormat.add("%d");
    colHeaders.add("H2O cluster status"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("H2O internal security"); colTypes.add("string"); colFormat.add("%s");
    colHeaders.add("H2O API Extensions"); colTypes.add("string"); colFormat.add("%s");

    H2ONode[] members = H2O.CLOUD.members();
    long freeMem = 0;
    int totalCores = 0;
    int clusterAllowedCores = 0;
    int unhealthlyNodes = 0;
    boolean locked = Paxos._cloudLocked;
    for (int i = 0; i < members.length; i++) {
      freeMem += members[i]._heartbeat.get_free_mem();
      totalCores += members[i]._heartbeat._num_cpus;
      clusterAllowedCores += members[i]._heartbeat._cpus_allowed;
      if (!members[i].isHealthy()) unhealthlyNodes++;
    }
    String status = locked ? "locked" : "accepting new members";
    status += unhealthlyNodes > 0 ? ", " + unhealthlyNodes + " nodes are not healthly" : ", healthly";
    String apiExtensions = "";
    for (RestApiExtension ext : ExtensionManager.getInstance().getRestApiExtensions()) {
      if (apiExtensions.isEmpty())
        apiExtensions += ext.getName();
      else
        apiExtensions += ", " + ext.getName();
    }

    final int rows = 1;
    TwoDimTable table = new TwoDimTable(
            "Cluster Configuration", null,
            new String[rows],
            colHeaders.toArray(new String[0]),
            colTypes.toArray(new String[0]),
            colFormat.toArray(new String[0]),
            "");
    int row = 0;
    int col = 0;
    table.set(row, col++, System.currentTimeMillis() - H2O.START_TIME_MILLIS.get());
    table.set(row, col++, DateTimeZone.getDefault().toString());
    table.set(row, col++, ParseTime.getTimezone().toString());
    table.set(row, col++, H2O.ABV.projectVersion());
    table.set(row, col++, PrettyPrint.toAge(H2O.ABV.compiledOnDate(), new Date()));
    table.set(row, col++, H2O.ARGS.name);
    table.set(row, col++, H2O.CLOUD.size());
    table.set(row, col++, freeMem);
    table.set(row, col++, totalCores);
    table.set(row, col++, clusterAllowedCores);
    table.set(row, col++, status);
    table.set(row, col++, Boolean.toString(H2OSecurityManager.instance().securityEnabled));
    table.set(row, col++, apiExtensions);

    return table;
  }
}
