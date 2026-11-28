"""Windows CPU-set restriction works even on machines with >64 logical CPUs."""
import os,ctypes,struct

def restrict(pid=None,count=8):
    if os.name!='nt':return
    k=ctypes.WinDLL('kernel32',use_last_error=True);size=ctypes.c_ulong()
    k.GetCurrentProcess.restype=ctypes.c_void_p;k.OpenProcess.restype=ctypes.c_void_p
    process=k.GetCurrentProcess() if pid is None else k.OpenProcess(0x0200|0x1000,False,pid)
    if not process:return
    k.GetSystemCpuSetInformation(None,0,ctypes.byref(size),None,0)
    buf=ctypes.create_string_buffer(size.value)
    if not k.GetSystemCpuSetInformation(buf,size,ctypes.byref(size),None,0):raise ctypes.WinError(ctypes.get_last_error())
    ids=[];offset=0
    while offset<size.value:
        length,kind=struct.unpack_from('II',buf.raw,offset)
        if kind==0:ids.append(struct.unpack_from('I',buf.raw,offset+8)[0])
        offset+=length
    values=(ctypes.c_ulong*min(count,len(ids)))(*ids[:count])
    ok=k.SetProcessDefaultCpuSets(ctypes.c_void_p(process),values,len(values))
    if pid is not None:k.CloseHandle(ctypes.c_void_p(process))
    if not ok:raise ctypes.WinError(ctypes.get_last_error())

if __name__=='__main__':
    import sys
    restrict(int(sys.argv[1]) if len(sys.argv)>1 else None)
