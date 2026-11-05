# Q317: Clock Synchronization - Cristian's Algorithm
# Domain: Smart Serve: Food Demand Forecasting & Waste Reduction
#
# CSV File: cristian_clocks_317.csv
# client_id,t0_send_ms,t_server_ms,t1_recv_ms
# C1,1000,1050,1100
# C2,3790,3804,3868
# C3,2710,2723,2896
# C4,1700,1768,1856
# C5,3520,3573,3540
#
# T0 = time client sent the request, T_server = time reported by the time server,
# T1 = time client received the reply (all in ms on the client's own clock).

import csv

FILE_NAME = "cristian_clocks_317.csv"


def main():
    print("Cristian's Clock Synchronization - Smart Serve: Food Demand Forecasting & Waste Reduction")
    print()

    try:
        f = open(FILE_NAME, "r")
    except FileNotFoundError:
        print("Could not open " + FILE_NAME)
        return

    reader = csv.reader(f)
    next(reader)

    best_client = ""
    best_rtt = -1
    count = 0

    for row in reader:
        if len(row) < 4:
            continue
        client_id = row[0].strip()
        t0 = int(row[1].strip())
        ts = int(row[2].strip())
        t1 = int(row[3].strip())

        print("Client " + client_id + ":")
        print(f"  T0 = {t0} ms, T_server = {ts} ms, T1 = {t1} ms")

        if t1 < t0:
            print("  Invalid record (T1 is before T0), skipped")
            print()
            continue

        rtt = t1 - t0
        half = rtt / 2
        estimated = ts + half
        offset = estimated - t1

        print(f"  RTT = T1 - T0 = {t1} - {t0} = {rtt} ms")
        print(f"  Estimated server time = T_server + (RTT / 2) = {ts} + {half:.1f} = {estimated:.1f} ms")
        print(f"  Clock offset = {estimated:.1f} - {t1} = {offset:+.1f} ms")
        if offset > 0:
            print(f"  Client clock is behind the server by {offset:.1f} ms -> move it forward")
        elif offset < 0:
            print(f"  Client clock is ahead of the server by {-offset:.1f} ms -> slow it down / set it back")
        else:
            print("  Client clock is already in sync with the server")
        print(f"  Adjusted client time = {t1} + ({offset:+.1f}) = {estimated:.1f} ms  (accuracy +/- {half:.1f} ms)")
        print()

        count = count + 1
        if best_rtt == -1 or rtt < best_rtt:
            best_rtt = rtt
            best_client = client_id
    f.close()

    if count == 0:
        print("No valid records found")
    else:
        print(f"Clients synchronized = {count}")
        print(f"Most accurate synchronization: {best_client} (lowest RTT = {best_rtt} ms)")


main()
