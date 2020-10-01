package biz.dealnote.messenger.util

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.work.*
import biz.dealnote.messenger.Account_Types
import biz.dealnote.messenger.Constants
import biz.dealnote.messenger.Injection
import biz.dealnote.messenger.R
import biz.dealnote.messenger.api.HttpLogger
import biz.dealnote.messenger.api.ProxyUtil
import biz.dealnote.messenger.domain.InteractorFactory
import biz.dealnote.messenger.longpoll.AppNotificationChannels
import biz.dealnote.messenger.longpoll.NotificationHelper
import biz.dealnote.messenger.model.*
import biz.dealnote.messenger.player.util.MusicUtils
import biz.dealnote.messenger.settings.ISettings
import biz.dealnote.messenger.settings.Settings
import com.google.gson.Gson
import ealvatag.audio.AudioFileIO
import ealvatag.tag.FieldKey
import ealvatag.tag.Tag
import ealvatag.tag.id3.ID3v11Tag
import ealvatag.tag.id3.ID3v1Tag
import ealvatag.tag.images.ArtworkFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DownloadWorkUtils {
    @SuppressLint("ConstantLocale")
    private val DOWNLOAD_DATE_FORMAT: DateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private fun createNotification(context: Context, Title: String?, Text: String?, icon: Int, fin: Boolean): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, AppNotificationChannels.DOWNLOAD_CHANNEL_ID).setContentTitle(Title)
                .setContentText(Text)
                .setSmallIcon(icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(fin)
                .setOngoing(!fin)
                .setOnlyAlertOnce(true)
    }

    private fun createNotificationManager(context: Context): NotificationManagerCompat {
        val mNotifyManager = NotificationManagerCompat.from(context)
        if (Utils.hasOreo()) {
            mNotifyManager.createNotificationChannel(AppNotificationChannels.getDownloadChannel(context))
        }
        return mNotifyManager
    }

    private val ILLEGAL_FILENAME_CHARS = charArrayOf('#', '%', '&', '{', '}', '\\', '<', '>', '*', '?', '/', '$', '\'', '\"', ':', '@', '`', '|', '=')
    private fun makeLegalFilenameNTV(filename: String): String {
        var filename_temp = filename.trim { it <= ' ' }

        var s = '\u0000'
        while (s < ' ') {
            filename_temp = filename_temp.replace(s, '_')
            s++
        }
        for (i in ILLEGAL_FILENAME_CHARS.indices) {
            filename_temp = filename_temp.replace(ILLEGAL_FILENAME_CHARS[i], '_')
        }
        return filename_temp
    }

    @JvmStatic
    fun makeLegalFilename(filename: String, extension: String?): String {
        var result = makeLegalFilenameNTV(filename)
        if (result.length > 90) result = result.substring(0, 90).trim { it <= ' ' }
        if (extension == null)
            return result
        return "$result.$extension"
    }

    private fun optString(value: String): String {
        return if (Utils.isEmpty(value)) "" else value
    }

    @JvmStatic
    fun CheckDirectory(Path: String): Boolean {
        val dir_final = File(Path)
        return if (!dir_final.isDirectory) {
            dir_final.mkdirs()
        } else dir_final.setLastModified(Calendar.getInstance().time.time)
    }

    @Suppress("DEPRECATION")
    private fun toExternalDownloader(context: Context, url: String, file: DownloadInfo) {
        val downloadRequest = DownloadManager.Request(Uri.parse(url))
        downloadRequest.allowScanningByMediaScanner()
        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadRequest.setDescription(file.build_filename())
        downloadRequest.setDestinationUri(Uri.fromFile(File(file.build())))
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(downloadRequest)
    }

    private fun toDefaultInternalDownloader(context: Context, url: String, file: DownloadInfo) {
        val downloadWork = OneTimeWorkRequest.Builder(DefaultDownloadWorker::class.java)
        val data = Data.Builder()
        data.putString(ExtraDwn.URL, url)
        data.putString(ExtraDwn.DIR, file.path)
        data.putString(ExtraDwn.FILE, file.file)
        data.putString(ExtraDwn.EXT, file.ext)
        downloadWork.setInputData(data.build())
        WorkManager.getInstance(context).enqueue(downloadWork.build())
    }

    @Suppress("DEPRECATION")
    private fun default_file_exist(context: Context, file: DownloadInfo): Boolean {
        val Temp = File(file.build())
        if (Temp.exists()) {
            if (Temp.setLastModified(Calendar.getInstance().time.time)) {
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(Temp)))
            }
            PhoenixToast.CreatePhoenixToast(context).showToastError(R.string.exist_audio)
            return true
        }
        return false
    }

    @Suppress("DEPRECATION")
    private fun track_file_exist(context: Context, file: DownloadInfo): Int {
        val file_name = file.build_filename()
        val Temp = File(file.build())
        if (Temp.exists()) {
            if (Temp.setLastModified(Calendar.getInstance().time.time)) {
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(Temp)))
            }
            return 1
        }
        for (i in MusicUtils.RemoteAudios) {
            if (i == file_name)
                return 2
        }
        return 0
    }

    @JvmStatic
    fun TrackIsDownloaded(audio: Audio): Int {
        val audioName = makeLegalFilename(audio.artist + " - " + audio.title, "mp3")
        for (i in MusicUtils.CachedAudios) {
            if (i == audioName)
                return 1
        }
        for (i in MusicUtils.RemoteAudios) {
            if (i == audioName)
                return 2
        }
        return 0
    }

    @JvmStatic
    fun GetLocalTrackLink(audio: Audio): String {
        if (audio.url.contains("file://") || audio.url.contains("content://"))
            return audio.url
        return "file://" + Settings.get().other().musicDir + "/" + makeLegalFilename(audio.artist + " - " + audio.title, "mp3")
    }

    @JvmStatic
    fun doDownloadVideo(context: Context, video: Video, url: String, Res: String) {
        val result_filename = DownloadInfo(makeLegalFilename(optString(video.title) +
                " - " + video.ownerId + "_" + video.id + "_" + Res + "P", null), Settings.get().other().videoDir, "mp4")
        CheckDirectory(result_filename.path)
        if (default_file_exist(context, result_filename)) {
            return
        }
        try {
            if (!Settings.get().other().isUse_internal_downloader) {
                toExternalDownloader(context, url, result_filename)
            } else {
                toDefaultInternalDownloader(context, url, result_filename)
            }
        } catch (e: Exception) {
            PhoenixToast.CreatePhoenixToast(context).showToastError("Video Error: " + e.message)
            return
        }
    }

    @JvmStatic
    fun doDownloadVoice(context: Context, doc: VoiceMessage) {
        if (Utils.isEmpty(doc.linkMp3))
            return
        val result_filename = DownloadInfo(makeLegalFilename("Голосовуха " + doc.ownerId + "_" + doc.id, null), Settings.get().other().docDir, "mp3")
        CheckDirectory(result_filename.path)
        if (default_file_exist(context, result_filename)) {
            return
        }
        try {
            if (!Settings.get().other().isUse_internal_downloader) {
                toExternalDownloader(context, doc.linkMp3, result_filename)
            } else {
                toDefaultInternalDownloader(context, doc.linkMp3, result_filename)
            }
        } catch (e: Exception) {
            PhoenixToast.CreatePhoenixToast(context).showToastError("Voice Error: " + e.message)
            return
        }
    }

    private fun makeDoc(title: String, dir: String, ext: String?): DownloadInfo {
        var ext_i = Utils.firstNonEmptyString(ext, "doc")
        var file = title
        val pos = file.lastIndexOf('.')
        if (pos != -1) {
            ext_i = file.substring(pos + 1)
            file = file.substring(0, pos)
        }
        return DownloadInfo(file, dir, ext_i)
    }

    @JvmStatic
    fun doDownloadDoc(context: Context, doc: Document) {
        if (Utils.isEmpty(doc.url))
            return
        val result_filename = makeDoc(makeLegalFilename(doc.title, null), Settings.get().other().docDir, doc.ext)
        CheckDirectory(result_filename.path)
        if (default_file_exist(context, result_filename)) {
            return
        }
        try {
            if (!Settings.get().other().isUse_internal_downloader) {
                toExternalDownloader(context, doc.url, result_filename)
            } else {
                toDefaultInternalDownloader(context, doc.url, result_filename)
            }
        } catch (e: Exception) {
            PhoenixToast.CreatePhoenixToast(context).showToastError("Docs Error: " + e.message)
            return
        }
    }

    @JvmStatic
    fun doDownloadPhoto(context: Context, url: String, dir: String, file: String) {
        val result_filename = DownloadInfo(file, dir, "jpg")
        if (default_file_exist(context, result_filename)) {
            return
        }
        try {
            if (!Settings.get().other().isUse_internal_downloader) {
                toExternalDownloader(context, url, result_filename)
            } else {
                toDefaultInternalDownloader(context, url, result_filename)
            }
        } catch (e: Exception) {
            PhoenixToast.CreatePhoenixToast(context).showToastError("Photo Error: " + e.message)
            return
        }
    }

    @JvmStatic
    fun doDownloadAudio(context: Context, audio: Audio, account_id: Int, Force: Boolean): Int {
        if (!Utils.isEmpty(audio.url) && (audio.url.contains("file://") || audio.url.contains("content://")))
            return 3

        val result_filename = DownloadInfo(makeLegalFilename(audio.artist + " - " + audio.title, null), Settings.get().other().musicDir, "mp3")
        CheckDirectory(result_filename.path)
        val download_status = track_file_exist(context, result_filename)
        if (download_status != 0 && !Force) {
            return download_status
        }
        if (download_status == 1) {
            result_filename.setFile(result_filename.file + ("." + DOWNLOAD_DATE_FORMAT.format(Date())))
        }
        try {
            val downloadWork = OneTimeWorkRequest.Builder(TrackDownloadWorker::class.java)
            val data = Data.Builder()
            data.putString(ExtraDwn.URL, Gson().toJson(audio))
            data.putString(ExtraDwn.DIR, result_filename.path)
            data.putString(ExtraDwn.FILE, result_filename.file)
            data.putString(ExtraDwn.EXT, result_filename.ext)
            data.putInt(ExtraDwn.ACCOUNT, account_id)
            downloadWork.setInputData(data.build())
            WorkManager.getInstance(context).enqueue(downloadWork.build())
        } catch (e: Exception) {
            PhoenixToast.CreatePhoenixToast(context).showToastError("Audio Error: " + e.message)
            return 3
        }
        return 0
    }

    @JvmStatic
    fun makeDownloadRequestAudio(audio: Audio, account_id: Int): OneTimeWorkRequest {
        val result_filename = DownloadInfo(makeLegalFilename(audio.artist + " - " + audio.title, null), Settings.get().other().musicDir, "mp3")
        val downloadWork = OneTimeWorkRequest.Builder(TrackDownloadWorker::class.java)
        val data = Data.Builder()
        data.putString(ExtraDwn.URL, Gson().toJson(audio))
        data.putString(ExtraDwn.DIR, result_filename.path)
        data.putString(ExtraDwn.FILE, result_filename.file)
        data.putString(ExtraDwn.EXT, result_filename.ext)
        data.putInt(ExtraDwn.ACCOUNT, account_id)
        downloadWork.setInputData(data.build())

        return downloadWork.build()
    }

    open class DefaultDownloadWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
        protected fun show_notification(notification: NotificationCompat.Builder, id: Int, cancel_id: Int?) {
            if (cancel_id != null) {
                mNotifyManager.cancel(getId().toString(), cancel_id)
            }
            mNotifyManager.notify(getId().toString(), id, notification.build())
        }

        @Suppress("DEPRECATION")
        protected fun doDownload(url: String, file_v: DownloadInfo, UseMediaScanner: Boolean): Boolean {
            var mBuilder = createNotification(applicationContext,
                    applicationContext.getString(R.string.downloading), applicationContext.getString(R.string.downloading) + " "
                    + file_v.build_filename(), R.drawable.save, false)
            mBuilder.addAction(R.drawable.close, applicationContext.getString(R.string.cancel), WorkManager.getInstance(applicationContext).createCancelPendingIntent(id))

            show_notification(mBuilder, NotificationHelper.NOTIFICATION_DOWNLOADING, null)

            val file = file_v.build()
            try {
                FileOutputStream(file).use { output ->
                    if (Utils.isEmpty(url)) throw Exception(applicationContext.getString(R.string.null_image_link))
                    val builder = OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .addInterceptor(HttpLogger.DEFAULT_LOGGING_INTERCEPTOR).addInterceptor(object : Interceptor {
                                override fun intercept(chain: Interceptor.Chain): Response {
                                    val request = chain.request().newBuilder().addHeader("User-Agent", Constants.USER_AGENT(Account_Types.BY_TYPE)).build()
                                    return chain.proceed(request)
                                }
                            })
                    ProxyUtil.applyProxyConfig(builder, Injection.provideProxySettings().activeProxy)
                    val request: Request = Request.Builder()
                            .url(url)
                            .build()
                    val response = builder.build().newCall(request).execute()
                    if (!response.isSuccessful) {
                        throw java.lang.Exception("Server return " + response.code +
                                " " + response.message)
                    }
                    val bfr = response.body!!.byteStream()
                    val input = BufferedInputStream(bfr)
                    val data = ByteArray(80 * 1024)
                    var bufferLength: Int
                    var downloadedSize = 0.0
                    val cntlength = response.header("Content-Length")
                    var totalSize = 1
                    if (!Utils.isEmpty(cntlength)) totalSize = cntlength!!.toInt()
                    while (input.read(data).also { bufferLength = it } != -1) {
                        if (isStopped) {
                            output.flush()
                            input.close()
                            File(file).delete()
                            mNotifyManager.cancel(id.toString(), NotificationHelper.NOTIFICATION_DOWNLOADING)
                            return false
                        }
                        output.write(data, 0, bufferLength)
                        downloadedSize += bufferLength.toDouble()
                        mBuilder.setProgress(100, (downloadedSize / totalSize * 100).toInt(), false)
                        show_notification(mBuilder, NotificationHelper.NOTIFICATION_DOWNLOADING, null)
                    }
                    output.flush()
                    input.close()
                    if (UseMediaScanner) {
                        applicationContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(file))))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

                mBuilder = createNotification(applicationContext,
                        applicationContext.getString(R.string.downloading), applicationContext.getString(R.string.error)
                        + " " + e.localizedMessage + ". " + file_v.build_filename(), R.drawable.ic_error_toast_vector, true)
                mBuilder.color = Color.parseColor("#ff0000")
                show_notification(mBuilder, NotificationHelper.NOTIFICATION_DOWNLOAD, NotificationHelper.NOTIFICATION_DOWNLOADING)
                val result = File(file_v.build())
                if (result.exists()) {
                    file_v.setFile(file_v.file + "." + file_v.ext)
                    result.renameTo(File(file_v.setExt("error").build()))
                }
                Utils.inMainThread { PhoenixToast.CreatePhoenixToast(applicationContext).showToastError(R.string.error_with_message, e.localizedMessage) }
                return false
            }
            return true
        }

        @Suppress("DEPRECATION")
        private fun createForeground() {
            val builder: NotificationCompat.Builder
            builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("worker_channel", applicationContext.getString(R.string.channel_keep_work_manager),
                        NotificationManager.IMPORTANCE_NONE)
                mNotifyManager.createNotificationChannel(channel)
                NotificationCompat.Builder(applicationContext, channel.id)
            } else {
                NotificationCompat.Builder(applicationContext).setPriority(Notification.PRIORITY_MIN)
            }
            builder.setContentTitle(applicationContext.getString(R.string.work_manager))
                    .setContentText(applicationContext.getString(R.string.may_down_charge))
                    .setSmallIcon(R.drawable.web)
                    .setColor(Color.parseColor("#dd0000"))
                    .setOngoing(true)

            setForegroundAsync(ForegroundInfo(NotificationHelper.NOTIFICATION_DOWNLOAD_MANAGER, builder.build()))
        }

        override fun doWork(): Result {
            createForeground()

            val file_v = DownloadInfo(inputData.getString(ExtraDwn.FILE)!!,
                    inputData.getString(ExtraDwn.DIR)!!, inputData.getString(ExtraDwn.EXT)!!)

            val ret = doDownload(inputData.getString(ExtraDwn.URL)!!, file_v, true)
            if (ret) {
                val mBuilder = createNotification(applicationContext,
                        applicationContext.getString(R.string.downloading), applicationContext.getString(R.string.success)
                        + " " + file_v.build_filename(), R.drawable.save, true)
                mBuilder.color = Utils.getThemeColor(false)

                val intent_open = Intent(Intent.ACTION_VIEW)
                intent_open.setDataAndType(FileProvider.getUriForFile(applicationContext, Constants.FILE_PROVIDER_AUTHORITY, File(file_v.build())), MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(file_v.ext)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val ReadPendingIntent = PendingIntent.getActivity(applicationContext, id.hashCode(), intent_open, PendingIntent.FLAG_UPDATE_CURRENT)
                mBuilder.setContentIntent(ReadPendingIntent)

                show_notification(mBuilder, NotificationHelper.NOTIFICATION_DOWNLOAD, NotificationHelper.NOTIFICATION_DOWNLOADING)
                Utils.inMainThread { PhoenixToast.CreatePhoenixToast(applicationContext).showToastBottom(R.string.saved) }
            }
            return if (ret) Result.success() else Result.failure()
        }

        private val mNotifyManager: NotificationManagerCompat = createNotificationManager(applicationContext)

    }

    class TrackDownloadWorker(context: Context, workerParams: WorkerParameters) : DefaultDownloadWorker(context, workerParams) {
        override fun doWork(): Result {
            val file_v = DownloadInfo(inputData.getString(ExtraDwn.FILE)!!,
                    inputData.getString(ExtraDwn.DIR)!!, inputData.getString(ExtraDwn.EXT)!!)
            val audio = Gson().fromJson(inputData.getString(ExtraDwn.URL)!!, Audio::class.java)
            val account_id = inputData.getInt(ExtraDwn.ACCOUNT, ISettings.IAccountsSettings.INVALID_ID)
            if (Utils.isEmpty(audio.url) || audio.isHLS) {
                val link = RxUtils.BlockingGetSingle(InteractorFactory
                        .createAudioInteractor().getByIdOld(account_id, listOf(IdPair(audio.id, audio.ownerId))).map { e: List<Audio> -> e[0].url }, audio.url)
                if (!Utils.isEmpty(link)) {
                    audio.url = link
                }
            }

            val final_url = Audio.getMp3FromM3u8(audio.url)
            if (Utils.isEmpty(final_url)) {
                return Result.failure()
            }

            val ret = doDownload(final_url, file_v, true)
            if (ret) {

                val cover = Utils.firstNonEmptyString(audio.thumb_image_very_big, audio.thumb_image_little)
                var updated_tag = false
                if (!Utils.isEmpty(cover)) {
                    val cover_file = DownloadInfo(file_v.file, file_v.path, "jpg")
                    if (doDownload(cover, cover_file, false)) {
                        try {
                            val audioFile = AudioFileIO.read(File(file_v.build()))
                            var tag: Tag = audioFile.tagOrSetNewDefault
                            if (tag is ID3v1Tag || tag is ID3v11Tag) {
                                tag = audioFile.setNewDefaultTag(); }

                            val Cover = File(cover_file.build())
                            val newartwork = ArtworkFactory.createArtworkFromFile(Cover)
                            tag.setArtwork(newartwork)
                            if (!Utils.isEmpty(audio.artist))
                                tag.setField(FieldKey.ARTIST, audio.artist)
                            if (!Utils.isEmpty(audio.title))
                                tag.setField(FieldKey.TITLE, audio.title)
                            if (!Utils.isEmpty(audio.album_title))
                                tag.setField(FieldKey.ALBUM, audio.album_title)
                            if (audio.lyricsId != 0) {
                                val LyricString: String? = RxUtils.BlockingGetSingle(InteractorFactory.createAudioInteractor().getLyrics(account_id, audio.lyricsId), null)
                                if (Utils.isEmpty(LyricString)) {
                                    tag.setField(FieldKey.COMMENT, "{owner_id=" + audio.ownerId + "_id=" + audio.id + "}")
                                } else {
                                    tag.setField(FieldKey.COMMENT, "{owner_id=" + audio.ownerId + "_id=" + audio.id + "} " + LyricString)
                                }
                            } else {
                                tag.setField(FieldKey.COMMENT, "{owner_id=" + audio.ownerId + "_id=" + audio.id + "}")
                            }
                            audioFile.save()
                            Cover.delete()
                            updated_tag = true
                        } catch (e: Throwable) {
                            Utils.inMainThread { PhoenixToast.CreatePhoenixToast(applicationContext).showToastError(R.string.error_with_message, e.localizedMessage) }
                            e.printStackTrace()
                        }
                    }
                }

                val mBuilder = createNotification(applicationContext,
                        applicationContext.getString(if (updated_tag) R.string.tag_modified else R.string.downloading), applicationContext.getString(R.string.success)
                        + " " + file_v.build_filename(), R.drawable.save, true)
                mBuilder.color = Utils.getThemeColor(false)

                val intent_open = Intent(Intent.ACTION_VIEW)
                intent_open.setDataAndType(FileProvider.getUriForFile(applicationContext, Constants.FILE_PROVIDER_AUTHORITY, File(file_v.build())), MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(file_v.ext)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val ReadPendingIntent = PendingIntent.getActivity(applicationContext, id.hashCode(), intent_open, PendingIntent.FLAG_UPDATE_CURRENT)
                mBuilder.setContentIntent(ReadPendingIntent)

                show_notification(mBuilder, NotificationHelper.NOTIFICATION_DOWNLOAD, NotificationHelper.NOTIFICATION_DOWNLOADING)
                MusicUtils.CachedAudios.add(file_v.build_filename())
                Utils.inMainThread { PhoenixToast.CreatePhoenixToast(applicationContext).showToastBottom(if (updated_tag) R.string.tag_modified else R.string.saved) }
            }
            return if (ret) Result.success() else Result.failure()
        }
    }

    private object ExtraDwn {
        const val URL = "url"
        const val DIR = "dir"
        const val FILE = "file"
        const val EXT = "ext"
        const val ACCOUNT = "account"
    }

    class DownloadInfo(file: String, path: String, ext: String) {

        fun setFile(file: String): DownloadInfo {
            this.file = file
            return this
        }

        fun setExt(ext: String): DownloadInfo {
            this.ext = ext
            return this
        }

        fun build_filename(): String {
            return "$file.$ext"
        }

        fun build(): String {
            return "$path/$file.$ext"
        }

        var file: String = file
            private set
        var path: String = path
            private set
        var ext: String = ext
            private set
    }
}
