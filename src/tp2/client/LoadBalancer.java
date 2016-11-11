package tp2.client;

import tp2.shared.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by Rusty on 11/10/2016.
 */
public class LoadBalancer
{
    public static void main(String[] args)
    {
        for (String arg : args)
            System.out.println(arg);

        if (args.length != 2)
            throw new IllegalArgumentException();

        // Construire les requetes a etre executees
        String fileName = args[0];
        String fileContents = Utils.readFile(fileName);

        String[] lines = fileContents.split("\n");
        List<Operation> requests = new ArrayList<>(lines.length);
        for (String line : lines)
            requests.add(new Operation(line));

        // Charger le fichier de config pour les serveurs
        String configContents = Utils.readFile("config.cfg");
        String[] serverList = configContents.split("\n");

        int result;

        LoadBalancer balancer = new LoadBalancer(requests, serverList);
        if (args[1].equals("safe"))
            result = balancer.runSafe();
        else result = balancer.runUnsafe();

        System.out.println(Integer.toString(result));
    }

    private static ServerInterface[] connectToServers(String[] servers)
    {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        ServerInterface[] list = new ServerInterface[servers.length];

        for (int i = 0; i < list.length; i++)
            list[i] = loadServer(servers[i]);

        return list;
    }

    private static ServerInterface loadServer(String hostnamePair)
    {
        if (hostnamePair.isEmpty())
            throw new IllegalArgumentException();

        String[] pair = hostnamePair.split(":");
        if (pair.length != 2)
            throw new IllegalArgumentException();

        String hostname = pair[0];
        int port = Integer.parseInt(pair[1]);

        System.out.println("Hostname: " + hostname + ", port: " + port);

        ServerInterface result = null;

        try
        {
            Registry registry = LocateRegistry.getRegistry(hostname);
            System.out.println("Located the registry successfully.");
            result = (ServerInterface) registry.lookup("server");
        } catch (NotBoundException e) {
            System.err.println("Erreur: le nom '" + e.getMessage() + "' n'est pas defini dans le registre");
            System.exit(-1);
        } catch (RemoteException e) {
            System.err.println("Erreur: " + e.getMessage());
            System.err.println("TYPE NAME: " + e.getClass().getName());
            System.exit(-1);
        }

        return result;
    }

    private List<Operation> requestList;
    private ServerInterface[] serverList;
    private LoadBalancer(List<Operation> requests, String[] servers)
    {
        super();

        requestList = requests;
        System.out.println("SIZE: " + requests.size());
        serverList = connectToServers(servers);
    }

    private int runSafe()
    {
        final int qInit = 15;

        int[] impliedQ = new int[serverList.length];
        for (int i = 0; i < impliedQ.length; i++)
            impliedQ[i] = qInit;

        int sum = 0;

        ExecutorService threadPool = Executors.newFixedThreadPool(serverList.length);

        while (true) {
            int firstIndex = Utils.firstIndexWhereStatusNotEquals(requestList, Status.DONE);
            if (firstIndex == requestList.size())
                break;

            List<Future<Integer>> futureList = new ArrayList<>();
            for (int i = 0; i < serverList.length; i++) {
                ServerInterface server = serverList[i];
                int currentQ = impliedQ[i];

                int firstIndexTODO = Utils.firstIndexWhereStatusEquals(requestList, Status.TODO);
                int firstIndexDone = Utils.firstIndexWhereStatusEquals(requestList, Status.DONE, firstIndexTODO);

                firstIndexDone = firstIndexTODO + currentQ < firstIndexDone ? firstIndexTODO + currentQ : firstIndexDone;

		System.out.println("FIRST INDEX TODO: " + firstIndexTODO);
		System.out.println("FIRSTINDEXDONE: " + firstIndexDone);
                List<Operation> subList = new ArrayList<>();
                for (int j = firstIndexTODO; i < firstIndexDone; i++) {
                    Operation current = requestList.get(j);
                    current.status = Status.IN_PROGRESS;
                    subList.add(current);
                }

                ServerRequest request = new ServerRequest(server, subList, true);
                futureList.add(threadPool.submit(request));
            }

            while (!futureList.isEmpty()) {
                List<Future<Integer>> toRemove = new ArrayList<>();
                for (Future<Integer> future : futureList) {
                    if (future.isDone()) {
                        int result;
                        try {
                            result = future.get();
                        } catch (ExecutionException | InterruptedException ex) {
                            System.err.println("Error: " + ex.getMessage());
                            continue;
                        }

                        sum += result;
                        sum %= 4000;

                        toRemove.add(future);
                    }
                }

                for (Future<Integer> fut : toRemove)
                    futureList.remove(fut);
            }
        }

        return sum;
    }

    private int runUnsafe()
    {
        return -1;
    }
}
