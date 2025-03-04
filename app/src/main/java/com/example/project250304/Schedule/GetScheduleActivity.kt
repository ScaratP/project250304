package com.example.project250304.Schedule

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.project250304.data.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException


class GetScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScheduleScreen()
        }
    }
}

//webview登入，沒辦法預覽
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(url: String, onLoginSuccess: (String) -> Unit) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true // 啟用 DOM 存儲，避免某些網站無法正常運行

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    // 獲取 Cookie，確保網頁登入後可正確解析
                    view.evaluateJavascript("document.cookie") { value ->
                        onLoginSuccess(value)
                    }
                }
            }
            loadUrl(url)
        }
    })
}

//?
@Composable
fun ScheduleScreen() {
    var cookies by remember { mutableStateOf<String?>(null) }
    var scheduleList by remember { mutableStateOf<List<Schedule>>(emptyList()) }

    if (cookies == null) {
        WebViewScreen("https://infosys.nttu.edu.tw") {
            cookies = it
            CoroutineScope(Dispatchers.IO).launch {
                scheduleList = fetchCourseData(it)
            }
        }
    } else {
        CourseList(scheduleList)
    }
}

//顯示課表用ㄉ
@Composable
fun CourseList(schedules: List<Schedule>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("課程清單", style = MaterialTheme.typography.headlineLarge)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(schedules) { schedule ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = schedule.course, style = MaterialTheme.typography.bodyLarge)
                        Text(text = "日期: ${schedule.date}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "時間: ${schedule.startTime} - ${schedule.endTime}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "地點: ${schedule.isolation}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

//爬蟲抓資料
fun fetchCourseData(cookie: String): List<Schedule> {
    return try {
        val doc: Document = Jsoup.connect("https://infosys.nttu.edu.tw/n_CourseBase_Select/WeekCourseList.aspx?ItemParam=")
            .header("Cookie", cookie) // 設定 Cookie 以獲取個人課表
            .get()

        val scheduleList = mutableListOf<Schedule>()
        val rows = doc.select("tbody tr") // 選取表格內的所有行

        for ((index, row) in rows.withIndex()) {
            val cells = row.select("td")
            if (cells.size < 8) continue // 確保有足夠的欄位，避免解析錯誤

            val timeSlot = cells[0].text() // 取得時間區間，例如 "08:10 - 09:00"
            val timeParts = timeSlot.split(" - ")
            val startTime = timeParts.getOrNull(0) ?: "未知"
            val endTime = timeParts.getOrNull(1) ?: "未知"

            for (dayIndex in 1..7) { // 解析星期一到星期日的課程
                val cell = cells[dayIndex]
                val courseName = cell.select("span").text()
                val classroom = cell.ownText().trim() // 可能的教室名稱，通常在文本內

                if (courseName.isNotBlank()) {
                    scheduleList.add(
                        Schedule(
                            id = index * 10 + dayIndex, // 生成唯一 ID
                            course = courseName,
                            date = "星期$dayIndex",
                            startTime = startTime,
                            endTime = endTime,
                            isolation = classroom.ifBlank { "未知" } // 預設為 "未知" 如果沒有教室資訊
                        )
                    )
                }
            }
        }
        scheduleList
    } catch (e: IOException) {
        e.printStackTrace()
        emptyList()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScheduleScreen() {
    val sampleSchedules = listOf(
        Schedule(1, "數學", "星期一", "08:00", "09:30", "A101"),
        Schedule(2, "英文", "星期二", "10:00", "11:30", "B202"),
        Schedule(3, "物理", "星期三", "13:00", "14:30", "C303")
    )
    CourseList(sampleSchedules)
}
