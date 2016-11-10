package tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface utilisee pour interfacer aux serveurs.
 */
public interface ServerInterface extends Remote
{
    int executeRequests(Operation[] requests) throws RemoteException, OperationRefusedException;
}
