package org.iofstorm.konatravels;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length == 0 || args[0] == null) throw new IllegalStateException("path is not specified");
        String path = args[0];
        System.out.println("path=" + path);

        Context context = new Context();
        DataLoader dataLoader = new DataLoader(context, path);

        dataLoader.loadData();

        new KonaServer(context).listen(80);
    }
}
