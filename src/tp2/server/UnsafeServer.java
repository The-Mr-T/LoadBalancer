package tp2.server;

import tp2.shared.Operation;

import java.util.List;

/**
 * Created by Rusty on 11/10/2016.
 */
public class UnsafeServer extends Server
{
    private int _maliciousness;
    UnsafeServer(int port, int qi, int maliciousness)
    {
        super(port, qi);
        _maliciousness = maliciousness;
    }

    @Override
    protected int executeMaybeMalicious(List<Operation> opList)
    {
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
