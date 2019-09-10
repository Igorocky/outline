package org.igye.outline2.manager;

import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.stereotype.Component;

@RpcMethodsCollection
@Component
public class AdminUtils {
    @RpcMethod
    public void doBackup() throws InterruptedException {
        System.out.println("backup!!!");
        Thread.sleep(2000);
    }
}
