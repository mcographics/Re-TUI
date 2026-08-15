@file:Suppress("DEPRECATION")

package ohi.andre.consolelauncher.commands.main.raw

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ohi.andre.consolelauncher.R
import ohi.andre.consolelauncher.UIManager
import ohi.andre.consolelauncher.commands.CommandAbstraction
import ohi.andre.consolelauncher.commands.ExecutePack
import ohi.andre.consolelauncher.managers.notifications.NotificationService

class clear : CommandAbstraction {
    override fun exec(pack: ExecutePack): String? {
        NotificationService.requestDismissAll(pack.context)
        LocalBroadcastManager.getInstance(pack.context.applicationContext)
            .sendBroadcast(Intent(UIManager.ACTION_CLEAR))
        return null
    }

    override fun argType(): IntArray = intArrayOf()

    override fun priority(): Int = 2

    override fun helpRes(): Int = R.string.help_clear

    override fun onArgNotFound(pack: ExecutePack, indexNotFound: Int): String? = null

    override fun onNotArgEnough(pack: ExecutePack, nArgs: Int): String? = null
}
