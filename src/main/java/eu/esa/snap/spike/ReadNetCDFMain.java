package eu.esa.snap.spike;

public class ReadNetCDFMain {

    public static void main(String[] args) throws Throwable {

        try (HdfLibFFM hdfLibFFM = new HdfLibFFM()) {
            hdfLibFFM.initialize();

            long fileHandle = hdfLibFFM.openFile("C:\\Satellite\\PRISMA\\PRS_L2D_STD_20240402102837_20240402102842_0001.he5", 0, 0);
            System.out.println("open = " + fileHandle);

            int result = hdfLibFFM.closeFile(fileHandle);
            System.out.println("close = " + result);
        }

        // https://docs.oracle.com/en/java/javase/22/core/calling-c-library-function-foreign-function-and-memory-api.html
    }
}
