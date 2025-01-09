import csv
import sys
import matplotlib.pyplot as plt
import numpy as np
import itertools

# font sizes
SMALL_SIZE = 14
MEDIUM_SIZE = 16
BIGGER_SIZE = 22

plt.rc('font', size=MEDIUM_SIZE)          # controls default text sizes
plt.rc('axes', titlesize=MEDIUM_SIZE)     # fontsize of the axes title
plt.rc('axes', labelsize=MEDIUM_SIZE)    # fontsize of the x and y labels
plt.rc('xtick', labelsize=SMALL_SIZE)    # fontsize of the tick labels
plt.rc('ytick', labelsize=SMALL_SIZE)    # fontsize of the tick labels
plt.rc('legend', fontsize=SMALL_SIZE)    # legend fontsize
plt.rc('figure', titlesize=BIGGER_SIZE)  # fontsize of the figure title

def main():
    if len(sys.argv) > 1:
        type = sys.argv[1]
        print(type)
    else:
        type = None

    agent_counts = [[] for _ in range(3)]

    with open('exp1_L8_4_0.csv', 'r') as csvfile:
        reader = csv.reader(csvfile, delimiter=',')
        ticks = int(next(reader)[0])  # Read the first line to get ticks
        print(f"ticks = {ticks}")

        # Read rows until a specific condition is met
        for row in reader:
            # Check for the termination condition
            if row and row[0].strip() == 'Dominance frequency':
                break
            if row and row[0].strip() == 'ToM0':
                row = next(reader)
            if row and row[0] != '':
                agent_counts[0].append(int(row[0]))
            if row and row[1] != '':
                agent_counts[1].append(int(row[1]))
            if row and row[2] != '':
                agent_counts[2].append(int(row[2]))



    # Create the x-axis data
    a = list(range(0, ticks, 5))

    min_length = min(len(agent_counts[0]), len(agent_counts[1]), len(agent_counts[2]), len(a))
    for i in range(3):
        agent_counts[i] = agent_counts[i][:min_length]

    plt.figure(figsize=(10, 6))
    plt.plot(a, agent_counts[0], color='black')
    plt.plot(a, agent_counts[1], color='red', linestyle='dashed')
    plt.plot(a, agent_counts[2], color='blue', linestyle='dotted')
    plt.ylim(0.0, 60.0)
    plt.xlabel("Number of Ticks")
    plt.ylabel("Number of Agents")
    plt.legend(['ToM0', 'ToM1', 'ToM2'])

    if type is None:
        plt.title("hoi")
    elif type == '1':
        plt.title("EXP1: ToM Progression over Time,\n $\\lambda = 0.8$")

    plt.savefig("time_lapse_exp1_L8_4.png")

    plt.show()

if __name__ == '__main__':
    main()
