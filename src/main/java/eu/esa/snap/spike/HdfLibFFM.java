package eu.esa.snap.spike;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class HdfLibFFM implements AutoCloseable {

    private final Linker linker;

    private Arena arena;
    private MethodHandle hdfFileOpenHandle;
    private MethodHandle hdfFileCloseHandle;
    private SymbolLookup hdfLib;

    public HdfLibFFM() {
        linker = Linker.nativeLinker();
        arena = null;
        hdfFileOpenHandle = null;
        hdfFileCloseHandle = null;
    }

    public void initialize() {
        if (arena == null) {
            arena = Arena.ofConfined();
            hdfLib = SymbolLookup.libraryLookup("C:\\Users\\Tom\\.snap\\auxdata\\netcdf_natives\\11.0.0.0\\amd64\\hdf5.dll", arena);
        }
    }

    public long openFile(String path, int access_flags, int fapl_id) throws Throwable {
        if (hdfFileOpenHandle == null) {
            bindHdfFileOpen();
        }

        final MemorySegment pathSegment = arena.allocateFrom(path);
        return (long) hdfFileOpenHandle.invokeExact(pathSegment, access_flags, fapl_id);
    }

    public int closeFile(long fileId) throws Throwable {
        if (hdfFileCloseHandle == null) {
            bindHdfFileClose();
        }

        return (int) hdfFileCloseHandle.invokeExact(fileId);
    }



    public void close() {
        if (arena != null) {
            arena.close();
            arena = null;
        }

        hdfFileOpenHandle = null;
        hdfFileCloseHandle = null;
    }

    private void bindHdfFileOpen() {
        // find the method we want - here: open HDF file and get the memory address for the call
        final var hdfFileOpen = hdfLib.find("H5Fopen");
        if (hdfFileOpen.isEmpty()) {
            throw new IllegalStateException("cannot access H5Fopen");
        }
        final MemorySegment hdfFileOpenPointer = hdfFileOpen.get();

        // describe the function:
        // returns a long value
        // arg_0: a pointer ot an out-of-VM string (the path of the file)
        // arg_1: an integer (file open mode)
        // arg_2: an integer (File access property list identifier)
        final FunctionDescriptor functionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

        // get a method handle from the linker
        hdfFileOpenHandle = linker.downcallHandle(hdfFileOpenPointer, functionDescriptor);
    }

    private void bindHdfFileClose() {
        final var hdfFileClose = hdfLib.find("H5Fclose");
        if (hdfFileClose.isEmpty()) {
            throw new IllegalStateException("cannot access H5Fclose");
        }
        final MemorySegment hdfFileClosePointer = hdfFileClose.get();

        // describe the function:
        // returns an int value
        // arg_0: the file handle
        final FunctionDescriptor functionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        // get a method handle from the linker
        hdfFileCloseHandle = linker.downcallHandle(hdfFileClosePointer, functionDescriptor);
    }
}
