package jp.techacademy.shintaro.nakagawa.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private val ImgList = arrayListOf<Uri>()
    private var max_listnum = 0
    private var now_listnum = 0
    private var sp_flag = false
    private var control_flag = true

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0

    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        next_button.setOnClickListener {
            if (control_flag) {
                if (!sp_flag) {
                    now_listnum += 1

                    if (now_listnum == max_listnum) now_listnum = 0
                    imageView.setImageURI(ImgList[now_listnum])
                }
            } else {
                Snackbar.make(it, "その操作は許可されていません。", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        back_button.setOnClickListener{
            if (control_flag) {
                if (!sp_flag) {
                    now_listnum -= 1

                    if (now_listnum < 0) now_listnum = max_listnum - 1
                    imageView.setImageURI(ImgList[now_listnum])
                }
            } else {
                Snackbar.make(it, "その操作は許可されていません。", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        sp_button.setOnClickListener{
            if (control_flag) {
                sp_flag = !sp_flag

                if (!sp_flag) {
                    sp_button.text = "再生"

                    if (mTimer != null) {
                        mTimer!!.cancel()
                        mTimer = null
                    }
                    mTimerSec = 0
                } else {
                    sp_button.text = "停止"

                    if (mTimer == null) {
                        mTimer = Timer()
                        mTimer!!.schedule(object : TimerTask() {
                            override fun run() {
                                mTimerSec += 1

                                if (mTimerSec % 20 == 0) {
                                    now_listnum += 1
                                    if (now_listnum == max_listnum) now_listnum = 0

                                    mHandler.post {
                                        imageView.setImageURI(ImgList[now_listnum])
                                    }
                                }
                            }
                        }, 100, 100) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
                    }
                }
            } else {
                Snackbar.make(it, "その操作は許可されていません。", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    control_flag = false
                }
        }
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                ImgList.add(imageUri)
                max_listnum += 1
            } while (cursor.moveToNext())
        }
        cursor.close()

        imageView.setImageURI(ImgList[now_listnum])
    }
}