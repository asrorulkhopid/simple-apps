package com.dicoding.todoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.provider.Settings.System.getString
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.todoapp.R
import com.dicoding.todoapp.data.Task
import com.dicoding.todoapp.data.TaskRepository
import com.dicoding.todoapp.ui.detail.DetailTaskActivity
import com.dicoding.todoapp.utils.DateConverter
import com.dicoding.todoapp.utils.NOTIFICATION_CHANNEL_ID
import com.dicoding.todoapp.utils.TASK_ID

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val repository = TaskRepository.getInstance(ctx)
    private val channelName = inputData.getString(NOTIFICATION_CHANNEL_ID)

    private fun getPendingIntent(task: Task): PendingIntent? {
        val intent = Intent(applicationContext, DetailTaskActivity::class.java).apply {
            putExtra(TASK_ID, task.id)
        }
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
    }

    override fun doWork(): Result {
        //TODO 14 : If notification preference on, get nearest active task from repository and show notification with pending intent

        val pref = applicationContext.getString(R.string.pref_key_notify)

        val preference = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preference.getBoolean(pref,false)?.apply {
            val nearestActiveTask = repository.getNearestActiveTask()

            if(this){
                val pendingIntent = getPendingIntent(nearestActiveTask)

                val mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val mBuilder = NotificationCompat.Builder( applicationContext, channelName?:"")
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle(nearestActiveTask.title)
                    .setContentText(DateConverter.convertMillisToString(nearestActiveTask.dueDateMillis))

                if(channelName != null && VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    val channel = NotificationChannel(channelName, channelName, NotificationManager.IMPORTANCE_HIGH)
                    mBuilder.setChannelId(channelName)
                    mNotificationManager.createNotificationChannel(channel)
                }
                val notification = mBuilder.build()

                mNotificationManager.notify(1, notification)
            }
        }

        return Result.success()
    }

}
