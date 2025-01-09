import csv
import os
import sys
import matplotlib.pyplot as plt
import numpy as np
import itertools
import seaborn as sns

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

# global variables
n_exps = 0

exp_lengths_l2 = []
exp_lengths_l5 = []
exp_lengths_l8 = []

n_finished_exps_l2 = 0
n_finished_exps_l5 = 0
n_finished_exps_l8 = 0

neg_endings_l2 = []
neg_endings_l5 = []
neg_endings_l8 = []
for _ in range(9):
    neg_endings_l2.append([])
    neg_endings_l5.append([])
    neg_endings_l8.append([])

pi_gains_l2 = []
pi_gains_l5 = []
pi_gains_l8 = []
for _ in range(9):
    pi_gains_l2.append([])
    pi_gains_l5.append([])
    pi_gains_l8.append([])

ages_l2 = []
ages_l5 = []
ages_l8 = []
for _ in range(9):
    ages_l2.append([])
    ages_l5.append([])
    ages_l8.append([])

dom_freqs_l2 = []
dom_freqs_l5 = []
dom_freqs_l8 = []
for _ in range(3):
    dom_freqs_l2.append([])
    dom_freqs_l5.append([])
    dom_freqs_l8.append([])

distr_l2 = []
distr_l5 = []
distr_l8 = []
for _ in range(3):
    distr_l2.append([])
    distr_l5.append([])
    distr_l8.append([])

neg_lengths_l2 = []
neg_lengths_l5 = []
neg_lengths_l8 = []
for _ in range(9):
    neg_lengths_l2.append([])
    neg_lengths_l5.append([])
    neg_lengths_l8.append([])


# save functions
def save_exp_length(reader, l_value):
    global exp_lengths_l2, exp_lengths_l5, exp_lengths_l8
    global n_finished_exps_l2, n_finished_exps_l5, n_finished_exps_l8

    ticks = int(next(reader)[0])  # Read the first line to get ticks
    if l_value == 2:
        exp_lengths_l2.append(ticks)
        if ticks == 3750000:
            n_finished_exps_l2 += 1
    if l_value == 5:
        exp_lengths_l5.append(ticks)
        if ticks == 3750000:
            n_finished_exps_l5 += 1
    if l_value == 8:
        exp_lengths_l8.append(ticks)
        if ticks == 3750000:
            n_finished_exps_l8 += 1
    return

def save_distr(distr_row, l_value):
    global distr_l2, distr_l5, distr_l8

    distr_dict = {
        2: distr_l2,
        5: distr_l5,
        8: distr_l8
    }
    distr_dict[l_value][0].append(float(distr_row[0]))
    distr_dict[l_value][1].append(float(distr_row[1]))
    distr_dict[l_value][2].append(float(distr_row[2]))

    distr_l2 = distr_dict[2]
    distr_l5 = distr_dict[5]
    distr_l8 = distr_dict[8]

    return


def save_dom_freq(domfreq_row, l_value):
    global dom_freqs_l2, dom_freqs_l5, dom_freqs_l8

    dom_freqs_dict = {
        2: dom_freqs_l2,
        5: dom_freqs_l5,
        8: dom_freqs_l8
    }
    dom_freqs_dict[l_value][0].append(float(domfreq_row[0]))
    dom_freqs_dict[l_value][1].append(float(domfreq_row[1]))
    dom_freqs_dict[l_value][2].append(float(domfreq_row[2]))

    dom_freqs_l2 = dom_freqs_dict[2]
    dom_freqs_l5 = dom_freqs_dict[5]
    dom_freqs_l8 = dom_freqs_dict[8]

    return


def save_age(reader, age_row, l_value):
    global ages_l2, ages_l5, ages_l8

    ages_dict = {
            2: ages_l2,
            5: ages_l5,
            8: ages_l8
    }

    while not (age_row[0] == '' and age_row[1] == '' and age_row[2] == ''):
        if age_row[0] and age_row[0] != '':
            ages_dict[l_value][0].append(int(age_row[0]))
        if age_row[1] and age_row[1] != '':
            ages_dict[l_value][1].append(int(age_row[1]))
        if age_row[2] and age_row[2] != '':
            ages_dict[l_value][2].append(int(age_row[2]))

        age_row = next(reader)
        if not age_row:
            ages_l2 = ages_dict[2]
            ages_l5 = ages_dict[5]
            ages_l8 = ages_dict[8]
            break

    return


def save_neg_length(reader, neg_length_row, l_value):
    global neg_lengths_l2, neg_lengths_l5, neg_lengths_l8

    neg_lengths_dict = {
        2: neg_lengths_l2,
        5: neg_lengths_l5,
        8: neg_lengths_l8
    }

    while not all(cell == '' for cell in neg_length_row[:9]):
        for i in range(9):
            if neg_length_row[i] and neg_length_row[i] != '':
                neg_lengths_dict[l_value][i].append(int(neg_length_row[i]))

        neg_length_row = next(reader)
        if not neg_length_row:
            neg_lengths_l2 = neg_lengths_dict[2]
            neg_lengths_l5 = neg_lengths_dict[5]
            neg_lengths_l8 = neg_lengths_dict[8]
            break

    return



def save_neg_ending(reader, neg_ending_row, l_value):
    global neg_endings_l2, neg_endings_l5, neg_endings_l8

    neg_endings_dict = {
        2: neg_endings_l2,
        5: neg_endings_l5,
        8: neg_endings_l8
    }

    while not all(cell == '' for cell in neg_ending_row[:9]):
        for i in range(9):
            if neg_ending_row[i]:
                neg_endings_dict[l_value][i].append(int(neg_ending_row[i]))

        neg_ending_row = next(reader)
        if not neg_ending_row:
            neg_endings_l2 = neg_endings_dict[2]
            neg_endings_l5 = neg_endings_dict[5]
            neg_endings_l8 = neg_endings_dict[8]
            break
    return


def save_pi_gain(reader, pi_gain_row, l_value):
    global pi_gains_l2, pi_gains_l5, pi_gains_l8

    pi_gains_dict = {
        2: pi_gains_l2,
        5: pi_gains_l5,
        8: pi_gains_l8
    }

    while not all(cell == '' for cell in pi_gain_row[:9]):
        for i in range(9):
            if pi_gain_row[i]:
                gains = pi_gain_row[i].split('0-')
                gains[0] = gains[0] + "0"
                pi_gains_dict[l_value][i].append(gains)  # they are still strings here

        pi_gain_row = next(reader)
        if not pi_gain_row:
            pi_gains_l2 = pi_gains_dict[2]
            pi_gains_l5 = pi_gains_dict[5]
            pi_gains_l8 = pi_gains_dict[8]
            break
    return


# plot functions
def plot_exp_lengths():
    global n_exps
    global exp_lengths_l2, exp_lengths_l5, exp_lengths_l8
    global n_finished_exps_l2, n_finished_exps_l5, n_finished_exps_l8


    # #   histogram
    # bins = np.linspace(min(exp_lengths_l2 + exp_lengths_l5 + exp_lengths_l8),
    #                max(exp_lengths_l2 + exp_lengths_l5 + exp_lengths_l8)+1,
    #                20)
    # width = 0.8 * (bins[1] - bins[0]) / 3
    # print(f"max length of l2 is {max(exp_lengths_l2)}")
    #
    # # plot histograms for each variable
    # plt.hist(exp_lengths_l2, bins=bins-width, width=width, color='#0072B2', alpha=1, label='$\\lambda$ = 0.2')
    # plt.hist(exp_lengths_l5, bins=bins, width=width, color='#FF6F20', alpha=1, label='$\\lambda$ = 0.5')
    # plt.hist(exp_lengths_l8, bins=bins+width, width=width, color='#A83232', alpha=1, label='$\\lambda$ = 0.8')
    #
    # # labels/titles
    # plt.xlabel('Experiment length')
    # plt.ylabel('Frequency')
    # plt.title('Histogram of Experiment Lengths \n $\\lambda \\in$ [0.2, 0.5, 0.8]')
    # plt.legend()
    # plt.xlim([0,3751000])
    #
    # # show plot
    # plt.tight_layout()
    # plt.savefig('exp_lengths_hist.png', dpi=300)
    # plt.show()

    exp_lengths_dict = {
        2: exp_lengths_l2,
        5: exp_lengths_l5,
        8: exp_lengths_l8
    }

    #   violin graph
    # print number of finished experiments
    print("Number of MAX_LENGTH experiments: " +
          f"{n_finished_exps_l2}, {n_finished_exps_l5}, {n_finished_exps_l8}")

    # create a list of all lambda values and a corresponding list of experiment lengths
    lambda_values = []
    exp_lengths = []

    for l_value, lengths in exp_lengths_dict.items():
        lambda_values.extend([l_value/10] * len(lengths))  # repeat the lambda value for each length
        exp_lengths.extend(lengths)

    # create the violin plot
    plt.figure(figsize=(10, 6))
    sns.violinplot(x=lambda_values, y=exp_lengths,)

    # labels/titles
    plt.title("EXP2: Distribution of Experiment Lengths, \n $\\lambda \\in$ [0.2, 0.5, 0.8]")
    plt.xlabel("Lambda")
    plt.ylabel("Experiment lengths")
    plt.grid(axis='y', color='lightgray', linestyle='--', linewidth=0.5, alpha=0.7)
    plt.axhline(y=0, color='black',linewidth=0.5)
    plt.axhline(y=3750000, color='black', linewidth=0.5)
    # add max length to y axis
    current_yticks = plt.yticks()[0]
    new_ytick = 3750000
    all_yticks = list(current_yticks) + [new_ytick]
    plt.yticks(sorted(all_yticks))  # sort to keep ticks in order

    # show and save the plot
    plt.tight_layout()
    plt.savefig('exp_lengths.png', dpi=300)
    plt.show()

    return


def plot_distrs():
    global distr_l2, distr_l5, distr_l8
    global exp_lengths_l2, exp_lengths_l5, exp_lengths_l8

    lambdas = [2, 5, 8]

    distr_dict = {
        2: distr_l2,
        5: distr_l5,
        8: distr_l8
    }

    distr_one_type = {}
    distr_multiple_types = {}

    for l_value in lambdas:
        exp_lengths = eval(f'exp_lengths_l{l_value}')
        distr_one_type[l_value] = [[] for _ in range(3)] #list for  ToM0, ToM1, ToM2
        distr_multiple_types[l_value] = [[] for _ in range(3)]

        # filter distributions based on exp_lengths
        for i in range(len(exp_lengths)):
            if exp_lengths[i] < 3750000:
                for t in range(3):  #
                    distr_one_type[l_value][t].append(distr_dict[l_value][t][i])
            else:
                for t in range(3):
                    distr_multiple_types[l_value][t].append(distr_dict[l_value][t][i])

    # compute averages and standard errors
    avg_ToM_distr_one = {}
    se_ToM_distr_one = {}
    avg_ToM_distr_multiple = {}
    se_ToM_distr_multiple = {}

    for l_value in lambdas:
        if distr_one_type[l_value]:
            avg_ToM_distr_one[l_value] = [np.mean(distr_one_type[l_value][t]) if distr_one_type[l_value][t] else 0 for t in range(3)]
            se_ToM_distr_one[l_value] = [np.std(distr_one_type[l_value][t]) / np.sqrt(len(distr_one_type[l_value][t])) if distr_one_type[l_value][t] else 0 for t in range(3)]
        else:
            avg_ToM_distr_one[l_value] = [0, 0, 0]
            se_ToM_distr_one[l_value] = [0, 0, 0]

        if distr_multiple_types[l_value]:
            avg_ToM_distr_multiple[l_value] = [np.mean(distr_multiple_types[l_value][t]) if distr_multiple_types[l_value][t] else 0 for t in range(3)]
            se_ToM_distr_multiple[l_value] = [np.std(distr_multiple_types[l_value][t]) / np.sqrt(len(distr_multiple_types[l_value][t])) if distr_multiple_types[l_value][t] else 0 for t in range(3)]
        else:
            avg_ToM_distr_multiple[l_value] = [0, 0, 0]
            se_ToM_distr_multiple[l_value] = [0, 0, 0]

        print("\nAverage order distributions: ToM0, ToM1, ToM2")
        # print results for one type left
        print(f'l={l_value} (One Type Left): ', end='')
        print(', '.join(f'{avg:.4f} (SE: {se:.4f})' for avg, se in zip(avg_ToM_distr_one[l_value], se_ToM_distr_one[l_value])))

        # print results for multiple types left
        print(f'l={l_value} (Multiple Types Left): ', end='')
        print(', '.join(f'{avg:.4f} (SE: {se:.4f})' for avg, se in zip(avg_ToM_distr_multiple[l_value], se_ToM_distr_multiple[l_value])))


    # data preprocessing
    labels = ['ToM0', 'ToM1', 'ToM2']

    # plot
    for condition in [("EXP2: Average Final Distribution (one type left), \n $\\lambda$ $\\in$ [0.2, 0.5, 0.8]",
                      [avg_ToM_distr_one[2], avg_ToM_distr_one[5], avg_ToM_distr_one[8]],
                      [se_ToM_distr_one[2], se_ToM_distr_one[5], se_ToM_distr_one[8]]),
                     ("EXP2: Average Final Distribution (multiple types left), \n $\\lambda$ $\\in$ [0.2, 0.5, 0.8]",
                      [avg_ToM_distr_multiple[2], avg_ToM_distr_multiple[5], avg_ToM_distr_multiple[8]],
                      [se_ToM_distr_multiple[2], se_ToM_distr_multiple[5], se_ToM_distr_multiple[8]])]:

        title, avg_values, se_values = condition
        x = np.arange(len(labels))
        width = 0.25

        fig, ax = plt.subplots(figsize=(10, 6))

        ax.bar(x - width, avg_values[0], width, label='$\\lambda$ = 0.2', color='#0072B2', yerr=se_values[0], capsize=5)
        ax.bar(x, avg_values[1], width, label='$\\lambda$ = 0.5', color='#FF6F20', yerr=se_values[1], capsize=5)
        ax.bar(x + width, avg_values[2], width, label='$\\lambda$ = 0.8', color='#A83232', yerr=se_values[2], capsize=5)

        # labels/titles
        ax.set_xlabel('ToM orders')
        ax.set_ylabel('Average Final Distribution')
        ax.set_title(title)
        ax.set_xticks(x)
        ax.set_xticklabels(labels)
        ax.legend()

        # show and save the plot
        plt.grid(axis='y', color='lightgray', linestyle='--', linewidth=0.5, alpha=0.7)
        plt.tight_layout()
        if title == "EXP2: Average Final Distribution (one type left), \n $\\lambda$ $\\in$ [0.2, 0.5, 0.8]":
            plt.savefig(f'avg_final_distr_1left.png', dpi=300)
        else:
            plt.savefig(f'avg_final_distr_2left.png', dpi=300)
        plt.show()

def plot_dom_freqs():
    global dom_freqs_l2, dom_freqs_l5, dom_freqs_l8

    lambdas = [2, 5, 8]

    dom_freqs_dict = {
        2: dom_freqs_l2,
        5: dom_freqs_l5,
        8: dom_freqs_l8
    }

    avg_ToM_freq = {}
    se_ToM_freq = {}

    # compute averages and standard errors for each lambda
    for l_value in lambdas:
        avg_ToM_freq[l_value] = [np.mean(dom_freqs_dict[l_value][i]) for i in range(3)]
        se_ToM_freq[l_value] = [np.std(dom_freqs_dict[l_value][i]) / np.sqrt(n_exps) for i in range(3)]

    # print averages of dominance frequencies
    print("\n Average dominance frequencies: ToM0, ToM1, ToM2")
    for l_value in lambdas:
        print(f"l={l_value}: " +
              ", ".join(f"{avg:.4f} (SE: {se:.4f})" for avg, se in zip(avg_ToM_freq[l_value], se_ToM_freq[l_value])))

    # prepare data
    labels = ['ToM0', 'ToM1', 'ToM2']
    avg_ToM_l2, avg_ToM_l5, avg_ToM_l8 = avg_ToM_freq[2], avg_ToM_freq[5], avg_ToM_freq[8]
    se_ToM_l2, se_ToM_l5, se_ToM_l8 = se_ToM_freq[2], se_ToM_freq[5], se_ToM_freq[8]

    x = np.arange(len(labels))
    width = 0.25

    # create the plot
    fig, ax = plt.subplots(figsize=(10, 6))

    # plotting each set of values for ToM0, ToM1, ToM2 with error bars
    ax.bar(x - width, avg_ToM_l2, width, label='$\\lambda$ = 0.2', color='#0072B2', yerr=se_ToM_l2, capsize=5)
    ax.bar(x, avg_ToM_l5, width, label='$\\lambda$ = 0.5', color='#FF6F20', yerr=se_ToM_l5, capsize=5)
    ax.bar(x + width, avg_ToM_l8, width, label='$\\lambda$ = 0.8', color='#A83232', yerr=se_ToM_l8, capsize=5)

    # labels/titles
    ax.set_xlabel('ToM orders')
    ax.set_ylabel('Average Dominance frequency')
    ax.set_title('EXP2: Average Dominance Frequencies for ToM0, ToM1, and ToM2, \n $\\lambda$ $\\in$ [0.2, 0.5, 0.8]')
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()

    # show the plot
    plt.grid(axis='y', color='lightgray', linestyle='--', linewidth=0.5, alpha=0.7)
    plt.tight_layout()
    plt.savefig('average_dom_freqs.png', dpi=300)
    plt.show()


    plot_zoomed_dom_freqs()
    return

def plot_zoomed_dom_freqs():
    global dom_freqs_l2, dom_freqs_l5, dom_freqs_l8

    lambdas = [2, 5, 8]

    dom_freqs_dict = {
        2: dom_freqs_l2,
        5: dom_freqs_l5,
        8: dom_freqs_l8
    }

    avg_ToM_freq = {}
    se_ToM_freq = {}

    # compute averages and standard errors for each lambda
    for l_value in lambdas:
        avg_ToM_freq[l_value] = [np.mean(dom_freqs_dict[l_value][i]) for i in range(1, 3)]  # skip index 0 (ToM0)
        se_ToM_freq[l_value] = [np.std(dom_freqs_dict[l_value][i]) / np.sqrt(n_exps) for i in range(1, 3)]

    # print averages of dominance frequencies
    print("\n Average dominance frequencies: ToM1, ToM2")
    for l_value in lambdas:
        print(f"l={l_value}: " +
              ", ".join(f"{avg:.4f} (SE: {se:.4f})" for avg, se in zip(avg_ToM_freq[l_value], se_ToM_freq[l_value])))

    # prepare data
    labels = ['ToM1', 'ToM2']  # exclude ToM0
    avg_ToM_l2, avg_ToM_l5, avg_ToM_l8 = avg_ToM_freq[2], avg_ToM_freq[5], avg_ToM_freq[8]
    se_ToM_l2, se_ToM_l5, se_ToM_l8 = se_ToM_freq[2], se_ToM_freq[5], se_ToM_freq[8]

    x = np.arange(len(labels))
    width = 0.25

    # create the plot
    fig, ax = plt.subplots(figsize=(10, 6))

    # plotting each set of values for ToM1 and ToM2 with error bars
    ax.bar(x - width, avg_ToM_l2, width, label='$\\lambda$ = 0.2', color='#0072B2', yerr=se_ToM_l2, capsize=5)
    ax.bar(x, avg_ToM_l5, width, label='$\\lambda$ = 0.5', color='#FF6F20', yerr=se_ToM_l5, capsize=5)
    ax.bar(x + width, avg_ToM_l8, width, label='$\\lambda$ = 0.8', color='#A83232', yerr=se_ToM_l8, capsize=5)

    # labels/titles
    ax.set_xlabel('ToM orders')
    ax.set_ylabel('Average Dominance Frequency')
    ax.set_title('EXP2: Average Dominance Frequencies for ToM1 and ToM2, \n $\\lambda$ $\\in$ [0.2, 0.5, 0.8]')
    ax.set_xticks(x)
    ax.set_xticklabels(labels)
    ax.legend()

    # show the plot
    plt.grid(axis='y', color='lightgray', linestyle='--', linewidth=0.5, alpha=0.7)
    plt.tight_layout()
    plt.savefig('avg_dom_freqs_Tom1_Tom2.png', dpi=300)
    plt.show()
    return

def plot_ages():
    global n_exps
    global ages_l2, ages_l5, ages_l8

    ages_dict = {
        2: ages_l2,
        5: ages_l5,
        8: ages_l8
    }

    for l_value in [2,5,8]:
        # create a list of all ToM orders and a corresponding list of agent ages
        tom_orders = []
        ages = []

        for order in range(3):
            tom_orders.extend([order] * len(ages_dict[l_value][order]))  # repeat the ToM order for each length
            ages.extend(ages_dict[l_value][order])

        # create the violin plot
        plt.figure(figsize=(10, 6))
        sns.violinplot(x=tom_orders, y=ages)

        # labels/titles
        plt.title(f"EXP2: Distribution of Ages per ToM Order, \n $\\lambda$ = {l_value/10}")
        plt.xlabel("ToM order")
        plt.ylabel("Agent ages")
        plt.axhline(y=0, color='black',linewidth=0.5)
        plt.grid(axis='y', color='lightgray', linestyle='--', linewidth=0.5, alpha=0.7)

        # show and save the plot
        plt.tight_layout()
        plt.savefig(f'ages_{l_value}.png', dpi=300)
        plt.show()

    pass

def plot_neg_lengths():
    global neg_lengths_l2, neg_lengths_l5, neg_lengths_l8

    # compute averages
    avg_neg_lengths_l2 = [np.mean(col) for col in neg_lengths_l2]
    avg_neg_lengths_l5 = [np.mean(col) for col in neg_lengths_l5]
    avg_neg_lengths_l8 = [np.mean(col) for col in neg_lengths_l8]

    # compute errors
    se_neg_lengths_l2 = [np.std(col)/np.sqrt(len(col)) for col in neg_lengths_l2]
    se_neg_lengths_l5 = [np.std(col)/np.sqrt(len(col)) for col in neg_lengths_l5]
    se_neg_lengths_l8 = [np.std(col)/np.sqrt(len(col)) for col in neg_lengths_l8]

    # print results
    print("\nAverage negotiation lengths per type: 0-0, 0-1, 0-2, 1-0, 1-1, 1-2, 2-0, 2-1, 2-2")
    print("l=0.2: " + ', '.join(f"{avg:.4f} (SE: {se:.4f})" if avg is not None else "None" for avg, se in
                                zip(avg_neg_lengths_l2, se_neg_lengths_l2)))
    print("l=0.5: " + ', '.join(f"{avg:.4f} (SE: {se:.4f})" if avg is not None else "None" for avg, se in
                                zip(avg_neg_lengths_l5, se_neg_lengths_l5)))
    print("l=0.8: " + ', '.join(f"{avg:.4f} (SE: {se:.4f})" if avg is not None else "None" for avg, se in

                                zip(avg_neg_lengths_l8, se_neg_lengths_l8)))
    # create the plot
    labels = ['0-0', '0-1', '0-2', '1-0', '1-1', '1-2', '2-0', '2-1', '2-2']
    x = np.arange(len(labels))
    width = 0.25
    fig, ax = plt.subplots(figsize=(10, 6))

    # plot bars for each lambda with error bars
    ax.bar(x - width, avg_neg_lengths_l2, width, yerr=se_neg_lengths_l2, label='$\\lambda$ = 0.2', color='#0072B2', capsize=5)
    ax.bar(x, avg_neg_lengths_l5, width, yerr=se_neg_lengths_l5,  label='$\\lambda$ = 0.5', color='#FF6F20', capsize=5)
    ax.bar(x + width, avg_neg_lengths_l8, width, yerr=se_neg_lengths_l8, label='$\\lambda$ = 0.8', color='#A83232', capsize=5)

    # labels/titles
    neg_types_dict = {
        0: 'ToM0-ToM0',
        1: 'ToM0-ToM1',
        2: 'ToM0-ToM2',
        3: 'ToM1-ToM0',
        4: 'ToM1-ToM1',
        5: 'ToM1-ToM2',
        6: 'ToM2-ToM0',
        7: 'ToM2-ToM1',
        8: 'ToM2-ToM2'
    }
    ax.set_xlabel('Negotiation types')
    ax.set_ylabel('Average negotiation length')
    ax.set_title('EXP2: Average Negotiation Length,\n $\\lambda$ $\\in$ [0.2, 0.5, 0.8]')
    ax.set_xticks(x)
    ax.set_xticklabels([neg_types_dict[j] for j in range(9)], rotation=45, ha='right')
    ax.legend()

    # show and save the plot
    plt.grid(axis='y', color='lightgray', linestyle='--', linewidth=0.5, alpha=0.7)
    plt.tight_layout()
    plt.savefig('neg_lengths_plot.png', dpi=300)
    plt.show()
    return


def plot_neg_endings():
    global neg_endings_l2, neg_endings_l5, neg_endings_l8

    neg_endings_dict = {
        2: neg_endings_l2,
        5: neg_endings_l5,
        8: neg_endings_l8
    }
    neg_types_dict = {
        0: 'ToM0-ToM0',
        1: 'ToM0-ToM1',
        2: 'ToM0-ToM2',
        3: 'ToM1-ToM0',
        4: 'ToM1-ToM1',
        5: 'ToM1-ToM2',
        6: 'ToM2-ToM0',
        7: 'ToM2-ToM1',
        8: 'ToM2-ToM2'
    }

    custom_labels = ["limit reached", "initiator withdrew", "other withdrew", "initiator accepted", "other accepted"]
    custom_colors = ['#0072B2','#FF6F20', '#A83232', '#4C8C8C', '#F0E442']
    for i in [2, 5, 8]:
        l_value = i
        data = neg_endings_dict[l_value]

        # matrix to store the counts per type
        ending_counts = np.zeros((9, len(custom_labels)))

        bins = [-1.5, -0.5, 0.5, 1.5, 2.5, 3.5]

        for j in range(9):
            flat_data = data[j]
            counts, _ = np.histogram(flat_data, bins=bins)
            ending_counts[j, :] = counts

        ending_percentages = (ending_counts / ending_counts.sum(axis=1, keepdims=True)) * 100

        # plot
        fig, ax = plt.subplots(figsize=(10, 6))
        bar_width = 0.5

        # stacked bar plot for each negotiation type
        bottom_values = np.zeros(9)
        x_positions = np.arange(9)

        for idx, (label, color) in enumerate(zip(custom_labels, custom_colors)):
            ax.bar(x_positions, ending_percentages[:, idx], bar_width, bottom=bottom_values, label=label, color=color, alpha=0.95)
            bottom_values += ending_percentages[:, idx]  # change starting height

        # labels/titles
        ax.set_xlabel("Negotiation Types")
        ax.set_ylabel("Relative Frequency (%)")
        ax.set_title(f"EXP2: Relative Frequency of Negotiation Endings by Type, \n $\\lambda$ = {l_value / 10}")
        ax.set_xticks(x_positions)
        ax.set_xticklabels([neg_types_dict[j] for j in range(9)], rotation=45, ha='right')
        plt.legend(loc='upper left', bbox_to_anchor=(1.05, 1), title="Reason for Termination")

        # show and save plot
        plt.grid(axis='y', color='lightgray', linestyle='--', linewidth=0.5, alpha=0.7)
        plt.tight_layout()
        plt.savefig(f'neg_endings_{i}.png', dpi=300)
        plt.show()

    return

def plot_pi_gains():
    global pi_gains_l2, pi_gains_l5, pi_gains_l8

    pi_gains_dict = {
        2: pi_gains_l2,
        5: pi_gains_l5,
        8: pi_gains_l8
    }

    neg_types_dict = {
        0: 'ToM0-ToM0',
        1: 'ToM0-ToM1',
        2: 'ToM0-ToM2',
        3: 'ToM1-ToM0',
        4: 'ToM1-ToM1',
        5: 'ToM1-ToM2',
        6: 'ToM2-ToM0',
        7: 'ToM2-ToM1',
        8: 'ToM2-ToM2'
    }

    for i in [2, 5, 8]:
        l_value = i

        initiator_avg = []
        initiator_se = []
        other_avg = []
        other_se = []

        for negotiation_type in pi_gains_dict[l_value]:
            initiator_gains = [float(pair[0]) for pair in negotiation_type]
            other_gains = [float(pair[1]) for pair in negotiation_type]
            # compute averages
            initiator_avg.append(np.mean(initiator_gains))
            initiator_se.append(np.std(initiator_gains)/np.sqrt(len(initiator_gains)))
            other_avg.append(np.mean(other_gains))
            other_se.append(np.std(other_gains)/np.sqrt(len(other_gains)))

        # print results
        print("\nAverage pi gain per negotiation type: 0-0, 0-1, 0-2, 1-0, 1-1, 1-2, 2-0, 2-1, 2-2")
        print("l=" + str(l_value) + ": " + ', '.join(
            f"initiator: {avg_init:.4f} (SE: {se_init:.4f}), other: {avg_other:.4f} (SE: {se_other:.4f})"
            if avg_init is not None else "None"
            for avg_init, se_init, avg_other, se_other in zip(initiator_avg, initiator_se, other_avg, other_se)
        ))

        bar_width = 0.35
        index = np.arange(9)

        # create the plot
        plt.figure(figsize=(10, 6))
        plt.bar(index, initiator_avg, bar_width, yerr=initiator_se, label='Initiator', color='#0072B2', capsize=5)
        plt.bar(index + bar_width, other_avg, bar_width, yerr=other_se, label='Other', color='#FF6F20',capsize=5)

        # labels/titles
        negotiation_labels = [neg_types_dict[i] for i in range(9)]
        plt.xlabel('Negotiation Types')
        plt.ylabel('Average Gains')
        plt.title(f'EXP2: Average Gains of Initiator vs Other by Negotiation Type, \n $ \\lambda $ = {l_value / 10}')
        plt.xticks(index + bar_width / 2, negotiation_labels, rotation=45)
        plt.ylim([0, 3])
        plt.legend()

        # show and save the plot
        plt.grid(axis='y', color='lightgray', linestyle='--', linewidth=0.5, alpha=0.7)
        plt.tight_layout()
        plt.savefig('pi_gains_plot'+str(i)+'.png', dpi=300)
        plt.show()

    return


def loop_files(filepath, l_value):
    with open(filepath, 'r') as csvfile:
        reader = csv.reader(csvfile, delimiter=',')
        save_exp_length(reader, l_value)

        for row in reader:
            if row and row[0] == 'ToM0':
                next(reader, None)
                distr_row = next(reader, None)
                save_distr(distr_row, l_value)
            if row and row[0] == ' Dominance frequency ':
                domfreq_row = next(reader, None)
                save_dom_freq(domfreq_row, l_value)
            if row and row[0] == '  Ages ':
                age_row = next(reader, None)
                save_age(reader, age_row, l_value)
            if row and row[0] == 'Negotiation lengths ':
                next(reader, None)
                neg_length_row = next(reader, None)
                save_neg_length(reader, neg_length_row, l_value)
            if row and row[0] == ' Negotiation Endings ':
                next(reader, None)
                neg_ending_row = next(reader, None)
                save_neg_ending(reader, neg_ending_row, l_value)
            if row and row[0] == ' Pi Gains (left is initiating agent':
                next(reader, None)
                pi_gain_row = next(reader, None)
                save_pi_gain(reader, pi_gain_row, l_value)
    return


def main():
    global n_exps
    n_exps = int(sys.argv[1])
    # n_exps = 5

    folder_name_1 = "exp2_l0.2"
    folder_name_2 = "exp2_l0.5"
    folder_name_3 = "exp2_l0.8"

    for i in range(0, n_exps):
        filename = "exp2_L2_" + str(i) + "_0.csv"
        filepath = os.path.join(folder_name_1, filename)
        loop_files(filepath, 2)

        filename = "exp2_L5_" + str(i) + "_0.csv"
        filepath = os.path.join(folder_name_2, filename)
        loop_files(filepath, 5)

        filename = "exp2_L8_" + str(i) + "_0.csv"
        filepath = os.path.join(folder_name_3, filename)
        loop_files(filepath, 8)

    plot_exp_lengths()
    plot_dom_freqs()
    plot_distrs()
    plot_ages()
    plot_neg_lengths()
    plot_neg_endings()
    plot_pi_gains()


if __name__ == '__main__':
    main()
