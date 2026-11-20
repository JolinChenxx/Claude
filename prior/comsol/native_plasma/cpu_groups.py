"""Inspect actual Windows thread processor groups for named COMSOL jobs."""
import ctypes as c
from ctypes import wintypes as w
from collections import Counter
import sys,json
from pathlib import Path

k=c.WinDLL('kernel32',use_last_error=True)
class ThreadEntry(c.Structure):
    _fields_=[('size',w.DWORD),('usage',w.DWORD),('tid',w.DWORD),('pid',w.DWORD),('base',w.LONG),('delta',w.LONG),('flags',w.DWORD)]
class GroupAffinity(c.Structure):
    _fields_=[('mask',c.c_size_t),('group',w.WORD),('reserved',w.WORD*3)]
k.CreateToolhelp32Snapshot.restype=w.HANDLE
k.Thread32First.argtypes=[w.HANDLE,c.POINTER(ThreadEntry)]
k.Thread32Next.argtypes=[w.HANDLE,c.POINTER(ThreadEntry)]
k.OpenThread.restype=w.HANDLE
k.GetThreadGroupAffinity.argtypes=[w.HANDLE,c.POINTER(GroupAffinity)]
k.CloseHandle.argtypes=[w.HANDLE]

def thread_ids(pid):
    snapshot=k.CreateToolhelp32Snapshot(4,0);entry=ThreadEntry();entry.size=c.sizeof(entry)
    ok=k.Thread32First(snapshot,c.byref(entry))
    while ok:
        if entry.pid==pid:yield entry.tid
        ok=k.Thread32Next(snapshot,c.byref(entry))
    k.CloseHandle(snapshot)

if __name__=='__main__':
    for name in sys.argv[1:]:
        pid=json.loads((Path(__file__).parent/'logs'/f'{name}_solve_process.json').read_text())['pid']
        counts=Counter()
        for tid in thread_ids(pid):
            handle=k.OpenThread(0x0040,False,tid);ga=GroupAffinity()
            if handle and k.GetThreadGroupAffinity(handle,c.byref(ga)):counts[(ga.group,hex(ga.mask))]+=1
            if handle:k.CloseHandle(handle)
        print(name,pid,dict(counts))
