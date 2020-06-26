package com.example.myapplication

import java.io.BufferedReader
import java.io.InputStreamReader

class ShellExecuter {

    fun Executer(command:String ):String{
        var output: StringBuffer  = StringBuffer()
        var  p: Process? =null
        var line : String  = ""
        try {
            p = Runtime.getRuntime().exec(command)
            p.waitFor()
            var reader: BufferedReader  = BufferedReader( InputStreamReader(p.getInputStream()))

            while (true) {
                output.append(line + "\n");
                reader.readLine()?:break
            }

        }catch ( e:Exception){
            e.printStackTrace()
        }
        return line
    }


}