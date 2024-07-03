package gg.rohan.beak.pterodactyl

import com.google.gson.annotations.SerializedName

class UploadUrlResponse {

    @SerializedName("object")
    internal lateinit var objectName: String

    @SerializedName("attributes")
    internal lateinit var attributes: Attributes

    internal class Attributes {
        @SerializedName("url")
        internal lateinit var url: String
    }

}