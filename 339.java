// Q339: RMI / RPC (Remote Invocation Simulation)
// Domain: Distributed File Storage System (DFS)
//
// CSV File: rpc_calls_339.csv
// call_id,client_name,method_name,param_value,timestamp
// 101,Client_A,processData,450,10:00:01
// 102,Client_B,validateRange,331,10:00:03
// 103,Client_C,computeSquare,87,10:00:04
// 104,Client_D,checkThreshold,65,10:00:06
// 105,Client_E,validateRange,1180,10:00:07
//
// Request format sent from client stub to server: call_id|method|param
// Reply format sent back by server:               call_id|OK|result  or  call_id|ERROR|reason

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class Server {

    // server stub: unmarshal the request, execute the method, marshal the reply
    String execute(String request) {
        System.out.println("[SERVER EXECUTION] Request received: " + request);
        String[] parts = request.split("\\|");
        String callId = parts[0];
        String method = parts[1];
        int value;

        try {
            value = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            System.out.println("[SERVER EXECUTION] Invalid parameter '" + parts[2] + "'");
            return callId + "|ERROR|BadParameter";
        }

        System.out.println("[SERVER EXECUTION] Unmarshalled -> method = " + method + ", parameter = " + value);

        String result;
        if (method.equals("processData")) {
            result = String.valueOf(value * 2);
            System.out.println("[SERVER EXECUTION] processData(" + value + ") = value * 2");
        } else if (method.equals("computeSquare")) {
            result = String.valueOf(value * value);
            System.out.println("[SERVER EXECUTION] computeSquare(" + value + ") = value * value");
        } else if (method.equals("checkThreshold")) {
            if (value >= 300) {
                result = "HIGH";
            } else {
                result = "NORMAL";
            }
            System.out.println("[SERVER EXECUTION] checkThreshold(" + value + ") compared with limit 300");
        } else if (method.equals("validateRange")) {
            if (value >= 0 && value <= 1000) {
                result = "VALID";
            } else {
                result = "OUT_OF_RANGE";
            }
            System.out.println("[SERVER EXECUTION] validateRange(" + value + ") checked against 0 - 1000");
        } else {
            System.out.println("[SERVER EXECUTION] Method '" + method + "' is not registered on the server");
            return callId + "|ERROR|NoSuchMethod";
        }

        return callId + "|OK|" + result;
    }
}

class Main {
    static final String FILE_NAME = "rpc_calls_339.csv";
    static Server server = new Server();

    // client stub: marshal the arguments and send them to the server
    static boolean client(String callId, String clientName, String method, String param, String time) {
        System.out.println("[DISPATCH] " + clientName + " invoking " + method + "(" + param + ") at " + time);
        String request = callId + "|" + method + "|" + param;
        System.out.println("[DISPATCH] Marshalled request: " + request);

        String reply = server.execute(request);

        String[] parts = reply.split("\\|");
        if (parts[1].equals("OK")) {
            System.out.println("[RETURN] Reply '" + reply + "' -> result for call " + parts[0] + " to " + clientName + " = " + parts[2]);
            return true;
        } else {
            System.out.println("[RETURN] Reply '" + reply + "' -> call " + parts[0] + " failed: " + parts[2]);
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("RPC / RMI Simulation - Distributed File Storage System (DFS)");
        System.out.println();

        int success = 0;
        int failed = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] row = line.split(",");
                if (row.length < 5) {
                    continue;
                }
                String callId = row[0].trim();
                String clientName = row[1].trim();
                String method = row[2].trim();
                String param = row[3].trim();
                String time = row[4].trim();

                if (client(callId, clientName, method, param, time)) {
                    success++;
                } else {
                    failed++;
                }
                System.out.println();
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Could not open " + FILE_NAME);
            return;
        }

        System.out.println("Total calls = " + (success + failed) + ", successful = " + success + ", failed = " + failed);
    }
}
