"""Assign unchanged independent reaction batches by measured solve duration."""
def distribute(tasks,workers,costs):
    assigned=[[] for _ in workers];loads=[0. for _ in workers]
    for item in sorted(tasks,key=lambda x:costs.get(f'{x[0]}:{x[1]}',1.),reverse=True):
        dest=min(range(len(workers)),key=loads.__getitem__)
        assigned[dest].append(item);loads[dest]+=max(costs.get(f'{item[0]}:{item[1]}',1.),.001)
    return assigned
