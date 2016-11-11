package tp2.shared;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Rusty on 11/10/2016.
 */
public class ServerRequest implements Callable<Integer>
{
    private ServerInterface server;
    private List<Operation> operations;
    private boolean isSafeMode;

    public ServerRequest(ServerInterface serverInterface, List<Operation> operationList, boolean safeMode)
    {
        server = serverInterface;
        operations = operationList;
        isSafeMode = safeMode;
    }

    public Integer call() throws Exception
    {
        Integer result = server.executeRequests(operations);
        for (Operation op : operations)
            if (isSafeMode)
                op.status = Status.DONE;
        return result;
    }
}
