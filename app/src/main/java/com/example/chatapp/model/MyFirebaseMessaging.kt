package com.example.chatapp.model

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.example.chatapp.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        //untuk nyimpen token
        val sented = remoteMessage.data["sented"]
        val user = remoteMessage.data["user"]
        val sharedPref = getSharedPreferences("PREF", Context.MODE_PRIVATE)
        val currentOnlineUser = sharedPref.getString("currentUser", "none")
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null && sented == firebaseUser.uid) {
            if (currentOnlineUser != user) {
                //kalo dr android Oreo keatas dia securitynya lebih bagus, jd ada permission tambahan
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage)
                } else {
                    //ini buat notif selain tmn2 oreo keatas
                    sendNotification(remoteMessage)
                }
            }
        }
    }

    //pop up kalo dapet pesan baru, nnt mengarah ruang obrolan
    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification
        val reg = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, ChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent =
            PendingIntent.getActivity(this, reg, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder: NotificationCompat.Builder = NotificationCompat.Builder ( this )
            .setSmallIcon(icon!!.toInt())
            .setContentTitle(title)
            .setContentText(body) //isi cuplikan pesannya yg muncul
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        //ini service dr notificationnya
        val notif = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //bikin index
        var i = 0
        if (reg > 0) {
            i = reg
        }
        notif.notify(i, builder.build())
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification
        val reg = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, ChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent =
            PendingIntent.getActivity(this, reg, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification(this)
        val builder: Notification.Builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon)
        var i = 0
        if (reg > 0) {
            i = reg
        }
        oreoNotification.getManager!!.notify(i, builder.build())
    }
}