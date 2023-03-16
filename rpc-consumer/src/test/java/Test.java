import com.corwin.rpc.api.IUserService;
import com.corwin.rpc.consumer.client.RpcClient;
import com.corwin.rpc.consumer.proxy.RpcClientProxy;

/**
 * @author dingsheng
 * @version 1.0
 * @description: TODO
 * @date 2023/3/6 15:22:42
 */
public class Test {

    @org.junit.jupiter.api.Test
    public void test() {
        IUserService userService = (IUserService) RpcClientProxy.createProxy(IUserService.class);
        System.out.println(userService.getAll());
    }

    @org.junit.jupiter.api.Test
    public void testConnect() {
        RpcClient rpcClient = new RpcClient("127.0.0.1", 8800);
    }
}
