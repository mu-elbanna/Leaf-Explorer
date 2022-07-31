/*
 * Copyright (C) 2019 Veli Tasalı
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.monora.uprotocol.client.android.config

import org.monora.uprotocol.client.android.fragment.ExplorerFragment

object AppConfig {
    const val SERVER_PORT_WEBSHARE = 58732

    const val DEFAULT_TIMEOUT_SOCKET = 5000

    const val DELAY_DEFAULT_NOTIFICATION = 1000

    const val EMAIL_DEVELOPER = "trebleshot@monora.org"

    const val URI_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.leaf.explorer"

    const val FILE_MANAGER_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.leaf.explorer.pro"

    const val URI_SHIV_SHAMBHU_DONATE = "https://try-tolearn.blogspot.com/2021/07/donate.html"

    const val URI_ORG_HOME = "https://github.com/Shiv-Shambhu"

    const val URI_REPO_APP = "https://github.com/Shiv-Shambhu/Leaf-Explorer"

    const val URI_GITHUB_UPROTOCOL = "https://github.com/uprotocol"

    const val URI_TRANSLATE = "https://hosted.weblate.org/engage/TrebleShot/"

    const val URI_TELEGRAM_CHANNEL = "https://t.me/Shiv_Shambhu_Github"

    const val PREFIX_ACCESS_POINT = "TS_"

    const val EXT_FILE_PART = "tshare"

    const val KEY_GOOGLE_PUBLIC = ("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk1peq7MhNms9ynhnoRtwxnb"
            + "izdEr3TKifUGlUPB3r33WkvPWjwowRvYeuY36+CkBmtjc46Xg/6/jrhPY+L0a+wd58lsNxLUMpo7"
            + "tN2std0TGrsMmmlihb4Bsxcu/6ThsY4CIQx0bdze2v8Zle3e4EoHuXcqQnpwkb+3wMx2rR2E9ih+"
            + "6utqrYAop9NdAbsRZ6BDXDUgJEuiHnRKwDZGDjU5PD4TCiR1jz2YJPYiRuI1QytJM6LirJu6YwE/"
            + "o6pfzSQ3xXlK4yGpGUhzLdTmSNQNIJTWRqZoM7qNgp+0ocmfQRJ32/6E+BxbJaVbHdTINhbVAvLR"
            + "+UFyQ2FldecfuQQIDAQAB")

    val DEFAULT_DISABLED_INTERFACES = arrayOf("rmnet")

    const val STORAGE_LOCATION = "STORAGE_LOCATION"

    const val CATEGORY = "CATEGORY"

    const val SHARE = "SHARE"

    const val INTENT_STORAGE_PATH = "intent_storage_path"

    const val BUFFER_LENGTH_DEFAULT = 4 * 1024 // IOUtils#DEFAULT_BUFFER_SIZE/8096

    const val DEFAULT_SOCKET_TIMEOUT = 5000

    const val SHOW_HIDDEN = "show_hidden"

    const val temporaryStopAds = "temporary_stop_ads"

    const val EXTRA_CATEGORY_TAB = "extraCategoryTab"

    const val CATEGORY_AUDIO = "Audios"
    const val CATEGORY_DOWNLOAD = "Downloads"
    const val CATEGORY_IMAGE = "Images"
    const val CATEGORY_VIDEO = "Videos"
    const val CATEGORY_DOCUMENT_OTHER = "Documents & other"
    const val CATEGORY_APP = "Apps"

    const val REFRESH_UPDATE = "ExplorerFragment.REFRESH_UPDATE"
}
