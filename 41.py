# Q41: RMI / RPC (Remote Invocation Simulation)
# Domain: Adaptive Distributed Computing Framework for Task Execution
#
# CSV File: rpc_calls_41.csv
# call_id,client_name,method_name,param_value,timestamp
# 101,Client_A,processData,450,10:00:01
# 102,Client_B,validateRange,949,10:00:03
# 103,Client_C,processData,410,10:00:06
# 104,Client_D,checkThreshold,525,10:00:09
# 105,Client_E,checkThreshold,400,10:00:10
#
# Request format sent from client stub to server: call_id|method|param
# Reply format sent back by server:               call_id|OK|result  or  call_id|ERROR|reason

import csv

FILE_NAME = "rpc_calls_41.csv"


def server(request):
    # server stub: unmarshal the request, execute the method, marshal the reply
    print("[SERVER EXECUTION] Request received: " + request)
    parts = request.split("|")
    call_id = parts[0]
    method = parts[1]

    try:
        value = int(parts[2])
    except ValueError:
        print("[SERVER EXECUTION] Invalid parameter '" + parts[2] + "'")
        return call_id + "|ERROR|BadParameter"

    print("[SERVER EXECUTION] Unmarshalled -> method = " + method + ", parameter = " + str(value))

    if method == "processData":
        result = value * 2
        print("[SERVER EXECUTION] processData(" + str(value) + ") = value * 2")
    elif method == "computeSquare":
        result = value * value
        print("[SERVER EXECUTION] computeSquare(" + str(value) + ") = value * value")
    elif method == "checkThreshold":
        if value >= 300:
            result = "HIGH"
        else:
            result = "NORMAL"
        print("[SERVER EXECUTION] checkThreshold(" + str(value) + ") compared with limit 300")
    elif method == "validateRange":
        if value >= 0 and value <= 1000:
            result = "VALID"
        else:
            result = "OUT_OF_RANGE"
        print("[SERVER EXECUTION] validateRange(" + str(value) + ") checked against 0 - 1000")
    else:
        print("[SERVER EXECUTION] Method '" + method + "' is not registered on the server")
        return call_id + "|ERROR|NoSuchMethod"

    return call_id + "|OK|" + str(result)


def client(call_id, client_name, method, param, time):
    # client stub: marshal the arguments and send them to the server
    print("[DISPATCH] " + client_name + " invoking " + method + "(" + param + ") at " + time)
    request = call_id + "|" + method + "|" + param
    print("[DISPATCH] Marshalled request: " + request)

    reply = server(request)

    parts = reply.split("|")
    if parts[1] == "OK":
        print("[RETURN] Reply '" + reply + "' -> result for call " + parts[0] + " to " + client_name + " = " + parts[2])
        return True
    else:
        print("[RETURN] Reply '" + reply + "' -> call " + parts[0] + " failed: " + parts[2])
        return False


def main():
    print("RPC / RMI Simulation - Adaptive Distributed Computing Framework for Task Execution")
    print()

    try:
        f = open(FILE_NAME, "r")
    except FileNotFoundError:
        print("Could not open " + FILE_NAME)
        return

    reader = csv.reader(f)
    next(reader)

    success = 0
    failed = 0
    for row in reader:
        if len(row) < 5:
            continue
        call_id = row[0].strip()
        client_name = row[1].strip()
        method = row[2].strip()
        param = row[3].strip()
        time = row[4].strip()

        if client(call_id, client_name, method, param, time):
            success = success + 1
        else:
            failed = failed + 1
        print()
    f.close()

    print("Total calls = " + str(success + failed) + ", successful = " + str(success) + ", failed = " + str(failed))


main()
