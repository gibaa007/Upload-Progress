package com.example.uploadprogress

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uploadprogress.ui.theme.UploadProgressTheme
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UploadProgressTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

//    var call by remember { mutableStateOf(null) }
    var netSpeed = 2
    var progress by remember { mutableStateOf(0f) }
    var fileUploaded by remember { mutableIntStateOf(0) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress/100,
        animationSpec = tween(durationMillis = 1000)
    )

    var fileName = "CGV_2140430003VideoCheck12mb"
    var fileSize by remember { mutableStateOf(0L) }
    var timeElapsed by remember { mutableStateOf("0s") }
    val context = LocalContext.current


    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()


        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://alpha-sotf-api.azurewebsites.net/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val file = getFileFromUri(fileName, context, uri!!)
        val requestBody = ProgressRequestBody(file, "video/*", object : ProgressCallback {
            override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
                fileUploaded = bytesUploaded.toInt()
                progress = ((100 * bytesUploaded / totalBytes).toInt()).toFloat()
                val time = (fileSize-fileUploaded)/netSpeed.toFloat()
                timeElapsed = convertSecondsToMinutes(time)
                Log.d("Upload Progress", progress.toString())
            }

            private fun convertSecondsToMinutes(seconds: Float): String {
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                return "${minutes.toInt()} m and ${remainingSeconds.toInt()} s"
            }
        })

        fileSize = file.length()
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val body2 = MultipartBody.Part.createFormData("file2", file.name, requestBody)
        var call = service.uploadFile(
            body,
            ChildDetailsID = 31362,
            FOID = 0,
            CommunityId = 446,
            ChildId = "2140430003",
            ContentType = "CGV",
            CreatedBy = 111,
            Source = "Mobile",
            SaveMode = "Online",
            Filename = fileName,
            FileType = "mp4"
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                Toast.makeText(context, "Upload Success", Toast.LENGTH_LONG).show()
                Log.d("Upload Success", response.message())
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Upload Failure", Toast.LENGTH_LONG).show()
                Log.d("Upload Failure", t.message.toString())
            }
        })
    }

    Column(
        modifier = Modifier
            .wrapContentWidth()
            .fillMaxHeight()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
    ) {


        Spacer(modifier = Modifier.height(320.dp))
        Button(onClick = { galleryLauncher.launch("video/*") }) {
            Text("Open Gallery")
        }
        Column(
            modifier = Modifier
                .width(600.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(start = 30.dp, end = 30.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier =
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) { }
                Column(
                    modifier =
                    Modifier.weight(3f), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CGV",
                        modifier = Modifier.padding(start = 5.dp),
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.Black
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                }
            }
            Spacer(modifier = Modifier.height(13.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp), horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    modifier = Modifier
                        .height(74.dp)
                        .width(74.dp),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "Play/Pause"
                )
                Column(
                    modifier = Modifier.weight(3f)
                ) {
                    Text(
                        text = fileName,
                        modifier = Modifier.padding(start = 5.dp),
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.Black
                    )
                    Row {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.LightGray, shape = CircleShape)
                            )
                            Text(
                                text = "$fileSize MB",
                                modifier = Modifier.padding(start = 5.dp, end = 10.dp),
                                style = TextStyle(fontSize = 16.sp),
                                color = Color.Black
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.LightGray, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
//                                text = "${fileUploaded/1000000} MB / $fileSize",
                                text = timeElapsed,
                                modifier = Modifier.padding(start = 5.dp),
                                style = TextStyle(fontSize = 16.sp),
                                color = Color.Black
                            )
                        }
                    }
                }
                Column(modifier = Modifier
                    .weight(0.5f)
                    .clickable {
//                        if(call.isExecuted)
//                        call.cancel()
                        Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show()
                    }
                )
                {
                    Box(
                        contentAlignment = Alignment.Center,  // Centers the pause icon
                        modifier = Modifier
                            .size(30.dp)  // Circle size
                            .border(
                                border = BorderStroke(
                                    2.dp,
                                    Color.Gray
                                ),  // Gray border
                                shape = CircleShape
                            )
                    ) {
                        Image(
                            modifier = Modifier
                                .height(15.dp)
                                .width(10.dp),
                            contentScale = ContentScale.Crop,
                            painter = painterResource(id = android.R.drawable.stat_sys_warning),
                            contentDescription = "Play/Pause",
                            colorFilter = ColorFilter.tint(Color.Red)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                Image(
                    modifier = Modifier
                        .height(24.dp)
                        .width(19.dp),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "Play/Pause",
                    colorFilter = ColorFilter.tint(Color.Red)
                )
                Text(
                    text = "Text",
                    modifier = Modifier.padding(start = 5.dp),
                    style = TextStyle(fontSize = 16.sp),
                    color = Color.Black
                )
            }
        }
    }
}

fun getFileFromUri(name: String, context: Context, contentUri: Uri): File {
    val fileExtension = getFileExtension(context, contentUri)
    val fileName = name + if (fileExtension != null) ".$fileExtension" else ""

    val tempFile = File(context.cacheDir, fileName)
    tempFile.createNewFile()

    try {
        val oStream = FileOutputStream(tempFile)
        val inputStream = context.contentResolver.openInputStream(contentUri)

        inputStream?.let {
            copy(inputStream, oStream)
        }

        oStream.flush()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return tempFile
}

private fun getFileExtension(context: Context, uri: Uri): String? {
    val fileType: String? = context.contentResolver.getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
}

@Throws(IOException::class)
private fun copy(source: InputStream, target: OutputStream) {
    val buf = ByteArray(8192)
    var length: Int
    while (source.read(buf).also { length = it } > 0) {
        target.write(buf, 0, length)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UploadProgressTheme {
        Greeting("Android")
    }
}