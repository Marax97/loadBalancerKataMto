package edu.iis.mto.serverloadbalancer;

import static edu.iis.mto.serverloadbalancer.CurrentLoadPercentageMatcher.hasLoadPercentageOf;
import static edu.iis.mto.serverloadbalancer.ServerBuilder.server;
import static edu.iis.mto.serverloadbalancer.VmBuilder.vm;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Enclosed.class)
public class ServerLoadBalancerParametrizedTest {

    @RunWith(Parameterized.class)
    public static class ServerFullyLoaded extends ServerLoadBalancerBaseTest {

        private int serverCapacity;
        private int vmSize;

        public ServerFullyLoaded(int serverCapacity, int vmSize) {
            this.serverCapacity = serverCapacity;
            this.vmSize = vmSize;
        }

        @Parameters
        public static Collection<Integer[]> initParamiters() {
            return Arrays.asList(new Integer[][] {{1, 1}, {2, 2}, {7, 7}});
        }

        @Test
        public void balancingOneServerWithOneSlotCapacity_andOneSlotVm_fillsTheServerWithTheVm() {
            Server theServer = a(server().withCapacity(serverCapacity));
            Vm theVm = a(vm().ofSize(vmSize));
            balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

            assertThat(theServer, hasLoadPercentageOf(100.0d));
            assertThat("the server should contain vm", theServer.contains(theVm));
        }
    }

    @RunWith(Parameterized.class)
    public static class ServersAndVms extends ServerLoadBalancerBaseTest {

        private List<Server> servers = new ArrayList<>();
        private List<Vm> vms = new ArrayList<>();
        Map<Integer, Integer> vmsAddedToServers;
        Map<Integer, Double> loadOfServers;

        public ServersAndVms(ParamiterKeeper param) {
            for (int i : param.listServersCapacity) {
                servers.add(a(server().withCapacity(i)));
            }

            for (int i : param.listVmsSize) {
                vms.add(a(vm().ofSize(i)));
            }

            vmsAddedToServers = param.vmsAddedToServers;
            loadOfServers = param.loadOfServers;
        }

        @Parameters
        public static Collection<ParamiterKeeper> initParamiters() {
            Map<Integer, Integer> vmsAddedToServers = new HashMap<Integer, Integer>() {

                {
                    put(1, 1);
                    put(2, 2);
                    put(3, 1);
                }
            };

            Map<Integer, Double> loadOfServers = new HashMap<Integer, Double>() {

                {
                    put(1, 75.0d);
                    put(2, 66.66d);
                }
            };
            ParamiterKeeper param1 = new ParamiterKeeper(Arrays.asList(4, 6), Arrays.asList(1, 4, 2), vmsAddedToServers, loadOfServers);
            return Arrays.asList(param1);
        }

        @Test
        public void balance_serversAndVms() {
            balance(aListOfServersWith(servers.toArray(new Server[0])), aListOfVmsWith(vms.toArray(new Vm[0])));

            for (Map.Entry<Integer, Integer> entry : vmsAddedToServers.entrySet()) {
                assertThat("The server" + entry.getValue() + "should contain the vm " + entry.getKey(), servers.get(entry.getKey() - 1)
                                                                                                               .contains(vms.get(
                                                                                                                       entry.getValue()
                                                                                                                                 - 1)));
            }
            //
            // assertThat(servers.get(0), hasLoadPercentageOf(75.0d));
            // assertThat(servers.get(1), hasLoadPercentageOf(66.66d));
        }

        private static class ParamiterKeeper {

            List<Integer> listServersCapacity;
            List<Integer> listVmsSize;
            Map<Integer, Integer> vmsAddedToServers;
            Map<Integer, Double> loadOfServers;

            public ParamiterKeeper(List<Integer> listServersCapacity, List<Integer> listVmsSize, Map<Integer, Integer> vmsAddedToServers,
                    Map<Integer, Double> loadOfServers) {
                super();
                this.listServersCapacity = listServersCapacity;
                this.listVmsSize = listVmsSize;
                this.vmsAddedToServers = vmsAddedToServers;
                this.loadOfServers = loadOfServers;
            }
        }
    }

}
