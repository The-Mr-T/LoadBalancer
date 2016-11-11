package tp2.server;

import tp2.shared.Operation;
import tp2.shared.ServerInterface;
import tp2.shared.OperationRefusedException;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Created by Rusty on 11/10/2016.
 */
public class Server implements ServerInterface
{
    public static void main(String[] args)
    {
        if (args.length != 3 && args.length != 4)
            throw new IllegalArgumentException();

        Server server = null;
        if (args[0].equals("safe"))
            server = new Server(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        else if (args[0].equals("unsafe"))
            server = new Server(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        else throw new IllegalArgumentException();

        server.run();
    }

    private int _port;
    private int _qi;
    private int _maliciousness;
    protected Server(int port, int qi)
    {
        super();

        _port = port;
        _qi = qi;
        _maliciousness = 0;
    }

    protected Server(int port, int qi, int maliciousness)
    {
        super();

        _port = port;
        _qi = qi;
        _maliciousness = maliciousness;
    }

    private void run()
    {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try
        {
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, _port);

            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("server", stub);
            System.out.println("Server ready.");
        } catch (ConnectException e) {
            System.err.println("Impossible de se connecer au registre RMI. Est-il actif?");

            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    public int executeRequests(List<Operation> opList) throws RemoteException, OperationRefusedException
    {
        int opCount = opList.size();
	System.out.println("RECEIVED REQUEST FOR " + opCount + " OPERATIONS");
        if (opCount > _qi)
        {
            double failProb = (opCount - _qi) / (4 * _qi);
            if (Math.random() < failProb)
                throw new OperationRefusedException();
        }

        if (Math.random() * 100 < _maliciousness)
            return (int)(Math.random() * Integer.MAX_VALUE);

        int sum = 0;

        for (Operation op : opList)
        {
            int result = op.execute();
            sum += result;
            sum %= 4000;
        }

        return sum;
    }
}
