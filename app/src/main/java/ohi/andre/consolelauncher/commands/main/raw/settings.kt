package ohi.andre.consolelauncher.commands.main.raw

import ohi.andre.consolelauncher.LauncherActivity
import ohi.andre.consolelauncher.R
import ohi.andre.consolelauncher.commands.CommandAbstraction
import ohi.andre.consolelauncher.commands.ExecutePack
import ohi.andre.consolelauncher.commands.main.MainPack
import ohi.andre.consolelauncher.commands.tuixt.ThemerActivity
import ohi.andre.consolelauncher.tuils.Tuils
import android.content.Intent
import android.provider.Settings

class settings : CommandAbstraction {
    override fun exec(pack: ExecutePack): String {
        // The bare, familiar `settings` command belongs to Android. Re:TUI's
        // editor remains available through `themer` and the in-app Settings UI.
        pack.context.startActivity(Intent(Settings.ACTION_SETTINGS))
        return Tuils.EMPTYSTRING
    }

    override fun argType(): IntArray = intArrayOf()

    override fun priority(): Int = 3

    override fun helpRes(): Int = R.string.help_settings

    override fun onArgNotFound(pack: ExecutePack, indexNotFound: Int): String = pack.context.getString(R.string.help_settings)

    override fun onNotArgEnough(pack: ExecutePack, nArgs: Int): String = exec(pack)

    companion object {
        private fun openSettings(pack: ExecutePack, section: String): String {
            val info = pack as MainPack
            val launcher = info.context as? LauncherActivity
            val uiManager = launcher?.uiManager ?: LauncherActivity.instance?.uiManager
            if (uiManager != null) {
                uiManager.openSettingsSurface(section)
                return Tuils.EMPTYSTRING
            }

            info.context.startActivity(ThemerActivity.launchIntent(info.context, section))
            return Tuils.EMPTYSTRING
        }
    }
}
