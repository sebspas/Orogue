package adapter;

import map.Map;
import map.Tuple;
import units.Master;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class Adapter extends Thread {

    private Socket socket;
    private int port;
    private BufferedWriter writer;
    private BufferedReader reader;
    private ListenSocket ls;
    private Master master;

    // new tiles seen by the unit, reset when we see it
    private ArrayList<Tuple<Integer, Integer>> tmpNewTiles;
    // pos enemy, if seen by the last unit else set to -1 -1
    private Tuple<Integer, Integer> posEnemy;

    //parameters width=xx height=yy

    private void initSocket() {
        try {
            socket = new Socket(InetAddress.getByName("localhost"), port);
            System.out.println("Connection Etablie !");
            reader =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private int extractX(String[] tab_line) {
        String[] buf = tab_line[1].split("=");
        return Integer.parseInt(buf[1]);
    }

    private int extractY(String[] tab_line) {
        String[] buf = tab_line[2].split("=");
        return Integer.parseInt(buf[1]);
    }

    private void processLine(String line) {
        String[] tab_line = line.split(" ");
        int x = extractX(tab_line);
        int y = extractY(tab_line);
        switch (tab_line[0]) {
            case "parameters":
                // x-> height, y -> width
                System.out.println("width " + x + " height " + y);
                master.setMap(new Map(y, x));
                Controller.getInstance().initGUI(y, x);
                break;
            case "print":
                updateMap(tab_line, x, y);
                // on ajoute a la cell a la liste des cell vue par cette unité

                if(tab_line[3].equals("ally") ) {
                    // c'est un ally on met a jour ca map de case vue
                    int id = Integer.parseInt(tab_line[4].split("=")[1]);
                    int hp = Integer.parseInt(tab_line[5].split("=")[1]);
                    char symbole = tab_line[6].split("=")[1].charAt(0);
                    master.updateEntity(id, hp, x, y,symbole);
                    // on met a jour les cases de cette unit
                    master.setNewTilesUnit(id, tmpNewTiles);
                    master.setPosEnemyByUnit(id, posEnemy);
                    // on reset les cases découverte
                    tmpNewTiles = new ArrayList<>();
                    posEnemy = new Tuple<>(-1, -1);
                } else if (tab_line[3].equals("ennemy")) {
                    // c'est la pos de l'ennemy vue par cette unit
                    posEnemy = new Tuple<>(x,y);
                } else {
                    // sinon c'est un terrain decouvert par l'unit
                    tmpNewTiles.add(new Tuple<>(x, y));
                }
                break;
            case "action?":
                if (!Controller.getInstance().isIA()) {
                    // we change the controller status to allow the user input (action)
                    Controller.getInstance().setStatus(Controller.Status.ACTION);
                    Controller.getInstance().getMapGUI().setFocus(x, y);
                } else {
                    Controller.getInstance().getMapGUI().setFocus(x, y);
                    int id = Integer.parseInt(tab_line[3].split("=")[1]);
                    System.out.println("Play unit " + id);
                    sendIAAction(master.playUnit(id), new Tuple<>(x, y));
                }

                // we need to see for which unit we need to move and stuff
                break;
            case "hide":
                master.hide(x, y);
                break;
        }
    }

    private void updateMap(String[] tab_line, int x, int y) {
        // the type : "terrain", "ally", "enemy"
        String type_terrain =  tab_line[3];

        if (tab_line[3].equals("ally") || tab_line[3].equals("ennemy")) {
            String[] buf = tab_line[6].split("=");

            // we update the map
            master.updateMap(x, y, type_terrain, buf[1]);
        } else {
            // the type , number of the type : mountain, water, ...
            String[] buf = tab_line[4].split("=");

            // we update the map
            master.updateMap(x, y, type_terrain, buf[1]);
        }
    }

    public void sendIAAction(Tuple<Integer, Integer> move, Tuple<Integer, Integer> posIA) {
        String action = "";
        System.out.println("Pos IA " + posIA.toString());
        System.out.println("Move IA " + move.toString());

        if (move.y - posIA.y == -1) {
            // north
            System.out.println("north");
            action = "north\n";
        } else if (move.y - posIA.y == 1) {
            // south
            System.out.println("south");
            action = "south\n";
        } else if (move.x - posIA.x == -1) {
            // east
            System.out.println("east");
            action = "east\n";
        } else if (move.x - posIA.x == 1) {
            // west
            System.out.println("west");
            action = "west\n";
        }
        else {
            System.out.println("Ne fait rien");
        }

        try {
            if(action != "") {
                writer.write(action);
                writer.flush();
                //Controller.getInstance().setStatus(Controller.Status.WAIT);
                Controller.getInstance().getMapGUI().setFocus(-1, -1);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAction(String key) {
        System.out.println("Key entered : " + key);
        String msg = "";
        switch (key) {
            case "Z":
                msg = "north\n";
                break;
            case "D":
                msg = "east\n";
                break;
            case "Q":
                msg = "west\n";
                break;
            case "S":
                msg = "south\n";
                break;
            case "NumPad-8":
            case "U":
                // ATTACK UP
                msg = "attack 0 -1\n";
                break;
            case "NumPad-6":
            case "K":
                //ATTACK RIGHT
                msg = "attack 1 0\n";
                break;
            case "NumPad-2":
            case "M":
                // ATTACK DOWN
                msg = "attack 0 1\n";
                break;
            case "NumPad-4":
            case "H":
                // ATTACK  LEFT
                msg = "attack -1 0\n";
                break;
        }
        try {
            if (!msg.equals("") && Controller.getInstance().getStatus() == Controller.Status.ACTION) {
                writer.write(msg);
                writer.flush();
                Controller.getInstance().setStatus(Controller.Status.WAIT);
                System.out.println(msg);
                Controller.getInstance().getMapGUI().setFocus(-1, -1);
            } else {
                System.out.println("Error of input or not the right time to move...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   /* private void updateAction() {

    }*/

    public Adapter(int port, Master master) {
        this.port = port;
        this.master = master;
        this.tmpNewTiles = new ArrayList<>();
        this.posEnemy = new Tuple<>(-1,-1);
        initSocket();
        System.out.println("Création de la socket de lecture");
        ls = new ListenSocket(reader);
        System.out.println("Socket de lecture crée");
        this.start();
        System.out.println("Thread de récupération de ligne lancé");
    }

    public void run() {
        String line;
        //int cpt = 0;
        while(true) {
            line = ls.getLastLine();
            System.out.println(line);
            processLine(line);
            //cpt++;
            //System.out.println(cpt);
        }
    }
}
