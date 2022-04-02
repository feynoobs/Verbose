package jp.co.fssoft.verbose.utility

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

/**
 * Json
 *
 * @constructor Create empty Json
 */
class Json
{
    companion object
    {
        /**
         * Tag
         */
        private val TAG = Json::class.qualifiedName

        /**
         * Json encode
         *
         * @param T
         * @param serializer
         * @param values
         * @return
         */
        public fun <T> jsonEncode(serializer: SerializationStrategy<T>, values: T): String
        {
            Log.v(TAG, "jsonEncode(${serializer}, ${values})")
            return Json { encodeDefaults = true }.encodeToString(serializer, values)
        }

        /**
         * Json list encode
         *
         * @param T
         * @param serializer
         * @param values
         * @return
         */
        public fun <T> jsonListEncode(serializer: KSerializer<List<T>>, values: List<T>): String
        {
            Log.v(TAG, "jsonListEncode(${serializer}, ${values})")
            return Json { encodeDefaults = true }.encodeToString(serializer, values)
        }

        /**
         * Json decode
         *
         * @param T
         * @param deserializer
         * @param json
         * @return
         */
        public fun <T> jsonDecode(deserializer: KSerializer<T>, json: String): T
        {
            Log.v(TAG, "jsonDecode(${deserializer}, ${json})")
            return Json {ignoreUnknownKeys = true; isLenient = true; useArrayPolymorphism = true}.decodeFromString(deserializer, json)
        }

        /**
         * Json list decode
         *
         * @param T
         * @param deserializer
         * @param json
         * @return
         */
        public fun <T> jsonListDecode(deserializer: KSerializer<List<T>>, json: String): List<T>
        {
            Log.v(TAG, "jsonDecode(${deserializer}, ${json})")
            return Json {ignoreUnknownKeys = true; isLenient = true; useArrayPolymorphism = true}.decodeFromString(deserializer, json)
        }
    }
}