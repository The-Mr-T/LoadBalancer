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
import java.util.Stack;

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
        
        boolean safety = args[1].equals("safe");
        long startTime = System.currentTimeMillis();
        LoadBalancer balancer = new LoadBalancer(requests, serverList, safety);
        result = balancer.run();
        long endTime = System.currentTimeMillis();
        
        long timeExec = endTime - startTime;

        System.out.println(Integer.toString(result));
        System.out.println("EXECUTION TIME: " + timeExec + " ms");
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

        String hostname = pair[0];
        int port = Integer.parseInt(pair[1]);

        System.out.println("Hostname: " + hostname + ", port: " + port);

        ServerInterface result = null;

        try
        {
            Registry registry = LocateRegistry.getRegistry(hostname, port);
            System.out.println("Located the registry successfully.");
            result = (ServerInterface) registry.lookup("server");
        } catch (NotBoundException e) {
            System.err.println("Erreur: le nom '" + e.getMessage() + "' n'est pas defini dans le registre");
            System.exit(-1);
        } catch (RemoteException e) {
            System.err.println("Erreur: " + e.getMessage());
            System.exit(-1);
        }

        return result;
    }

    private List<Operation> requestList;
    private ServerInterface[] serverList;
    private boolean isSafe;
    private Stack<ServerRequest> checkStack;
    private LoadBalancer(List<Operation> requests, String[] servers, boolean safety)
    {
        super();

        requestList = requests;
        System.out.println("SIZE: " + requests.size());
        serverList = connectToServers(servers);
        isSafe = safety;
        checkStack = new Stack<>();
    }

    private int run()
    {
        final int qInit = 15;

        int[] impliedQ = new int[serverList.length];
        for (int i = 0; i < impliedQ.length; i++)
            impliedQ[i] = qInit;

        int sum = 0;

        ExecutorService threadPool = Executors.newFixedThreadPool(serverList.length);

        while (true) {
            int doneIndex = Utils.firstIndexWhereStatusNotEquals(requestList, Status.DONE);
            if (doneIndex == requestList.size())
                break;
            
            List<Future<ServerRequest>> futureList = generateRequests(threadPool, impliedQ);
            sum = handleFutureList(futureList, impliedQ, sum);
        }

        return sum;
    }

    private List<Future<ServerRequest>> generateRequests(ExecutorService threadPool, int[] impliedQ) {
        List<Future<ServerRequest>> futureList = new ArrayList<>();
        for (int i = 0; i < serverList.length; i++) {
            ServerInterface server = serverList[i];
            int currentQ = impliedQ[i];

            if (!checkStack.isEmpty() && checkStack.peek().operations.size() <= currentQ)
            {
                futureList.add(threadPool.submit(checkStack.pop()));
                continue;
            }

            int firstIndex = Utils.firstIndexWhereStatusEquals(requestList, Status.TODO);
            int lastIndex = Utils.firstIndexWhereStatusNotEquals(requestList, Status.TODO, firstIndex);

            lastIndex = firstIndex + currentQ < lastIndex ? firstIndex + currentQ : lastIndex;
            
            List<Operation> subList = new ArrayList<>();
            for (int j = firstIndex; j < lastIndex; j++) {
                Operation current = requestList.get(j);
                current.status = Status.IN_PROGRESS;
                subList.add(current);
            }

            if (subList.size() == 0)
                futureList.add(null);
            else
            {
                ServerRequest request = new ServerRequest(server, subList);
                futureList.add(threadPool.submit(request));
            }
        }

        // Clean up the stack. If we didn't handle the verifications now then we probably never will
        while (!checkStack.isEmpty()) {
            ServerRequest unhandled = checkStack.pop();
            for (Operation op : unhandled.operations)
                op.status = Status.TODO;
        }
        return futureList;
    }

    private int handleFutureList(List<Future<ServerRequest>> futureList, int[] impliedQ, int theSum) {
        int sum = theSum;
        while (!Utils.allNull(futureList)) {
            List<Future<ServerRequest>> toRemove = new ArrayList<>();
            for (int i = 0; i < futureList.size(); i++) {
                Future<ServerRequest> future = futureList.get(i);
                    
                if (future == null)
                    continue;
                    
                if (future.isDone()) {
                    ServerRequest result;
                    try {
                        result = future.get();
                    } catch (ExecutionException | InterruptedException ex) {
                        System.err.println("Error: " + ex.getMessage());
                        continue;
                    }
                        
                    Status newStatus;
                    if (result.result == -1) {
                        impliedQ[i]--;
                        newStatus = Status.TODO;
                    }
                    else {
                        if (isSafe) {
                            newStatus = Status.DONE;
                            sum += result.result;
                            sum %= 4000;
                        }
                        else {
                            if (result.result == result.lastResult) {
                                newStatus = Status.DONE;
                                sum += result.result;
                                sum %= 4000;
                            }
                            else {
                                newStatus = Status.VERIFYING;
                                result.lastResult = result.result;
                                checkStack.push(result);
                            }
                        }
                    }

                    for (Operation op : result.operations)
                        op.status = newStatus;
                    futureList.set(i, null);
                }
            }
        }
        return sum;
    }
}
