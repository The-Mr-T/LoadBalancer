package tp2.server;

import tp2.shared.Operation;

import java.util.List;

/**
 * Created by Rusty on 11/10/2016.
 */
public class SafeServer extends Server
{
    SafeServer(int port, int qi)
    {
        super(port, qi);
    }

    @Override
    protected int executeMaybeMalicious(List<Operation> opList)
    {
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
