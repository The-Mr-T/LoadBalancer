package tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface utilisee pour interfacer aux serveurs.
 */
public interface ServerInterface extends Remote
{
    int executeRequests(List<Operation> requests) throws RemoteException, OperationRefusedException;
}
