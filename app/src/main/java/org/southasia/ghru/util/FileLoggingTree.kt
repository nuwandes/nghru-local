package org.southasia.ghru.util

import android.content.Context
import timber.log.Timber


class FileLoggingTree(private val context: Context) : Timber.DebugTree() {

//    override fun log(priority: Int, tag: String, message: String, t: Throwable?) {
//
//        try {
//
////            val direct = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ghru")
////
////            if (!direct.exists()) {
////                direct.mkdir()
////            }
//
//            val fileNameTimeStamp = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
//            val logTimeStamp = SimpleDateFormat("E MMM dd yyyy 'at' hh:mm:ss:SSS aaa", Locale.getDefault()).format(Date())
//
//            val fileName = "$fileNameTimeStamp.html"
//
//            //val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/ghru" + File.separator + fileName)
//
//            val path = Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_DOWNLOADS)
//            val file = File(path, fileName)
//            try {
//                // Make sure the Pictures directory exists.
//                path.mkdirs()
//
//                file.createNewFile()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//
//            if (file.exists()) {
//
//                val fileOutputStream = FileOutputStream(file, true)
//
//                fileOutputStream.write("<p style=\"background:lightgray;\"><strong style=\"background:lightblue;\">&nbsp&nbsp$logTimeStamp :&nbsp&nbsp</strong>&nbsp&nbsp$message</p>".toByteArray())
//                fileOutputStream.close()
//
//            }
//
//            //if (context != null)
//            //MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error while logging into file : $e")
//        }
//
//    }

    companion object {

        private val TAG = FileLoggingTree::class.java.simpleName
    }
}
