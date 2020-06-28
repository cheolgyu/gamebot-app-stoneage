package com.example.myapplication

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

class ShellExecuter {

    fun Executer(command:String ):String{
        Log.d("ShellExecuter", "shell Executer:"+command)
        var output: StringBuffer  = StringBuffer()
        var  p: Process? =null
        var str :StringBuilder =  StringBuilder()
        try {
            Log.d("ShellExecuter", "11")
            p = Runtime.getRuntime().exec(command)
            Log.d("ShellExecuter", "12")
            p.waitFor()
            Log.d("ShellExecuter", "13")
            var reader: BufferedReader  = BufferedReader( InputStreamReader(p.getInputStream()))

            while (true) {
                var _line =  reader.readLine()
                str.append(_line );
                _line?:break
            }
            Log.d("", "shell res="+str)

        }catch ( e:Exception){
           // e.printStackTrace()
            Log.e("", "err====================================="+e.printStackTrace().toString())
            return ""
        }
        return str.toString()
    }


}