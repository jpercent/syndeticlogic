package simpledb.server;

import simpledb.remote.*;
import java.rmi.*;

public class Startup {
   public static void main(String args[]) throws Exception {
      // configure and initialize the database
      SimpleDB.init(args[0]);
      
      // post the server entry in the rmi registry
      RemoteDriver d = new RemoteDriverImpl();
      Naming.rebind("simpledb", d);
      
      System.out.println("database server ready");
   }
}
