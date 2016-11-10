package tp2.client;

import tp2.shared.ServerRequest;
import tp2.shared.Utils;
import tp2.shared.Operation;
import tp2.shared.ServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
        if (args.length != 2)
            throw new IllegalArgumentException();

        // Construire les requetes a etre executees
        String fileName = args[1];
        String fileContents = Utils.readFile(fileName);

        String[] lines = fileContents.split("\n");
        Operation[] requests = new Operation[lines.length];
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i];
            requests[i] = new Operation(line);
        }

        // Charger le fichier de config pour les serveurs
        String configContents = Utils.readFile("config.cfg");
        String[] serverList = configContents.split("\n");

        LoadBalancer balancer = new LoadBalancer(requests, serverList);
        balancer.run();
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

    private static ServerInterface loadServer(String hostname)
    {
        ServerInterface result = null;

        try
        {
            Registry registry = LocateRegistry.getRegistry(hostname);
            result = (ServerInterface) registry.lookup("server");
        } catch (NotBoundException e) {
            System.err.println("Erreur: le nom '" + e.getMessage() + "' n'est pas defini dans le registre");
        } catch (RemoteException e) {
            System.err.println("Erreur: " + e.getMessage());
        }

        return result;
    }

    private Operation[] requestList;
    private ServerInterface[] serverList;
    private LoadBalancer(Operation[] requests, String[] servers)
    {
        super();

        requestList = requests;
        serverList = connectToServers(servers);
    }

    private int qInit = 15;
    private void run()
    {
        int[] impliedQ = new int[serverList.length];
        for (int i = 0; i < impliedQ.length; i++)
            impliedQ[i] = qInit;

        ExecutorService threadPool = Executors.newFixedThreadPool(serverList.length);

        List<Future<Integer>> futureList = new ArrayList<>();
        for (ServerInterface server : serverList)
        {

            //ServerRequest request = new ServerRequest(server, subList);
            //futureList.add(threadPool.submit(request));
        }
    }
}
