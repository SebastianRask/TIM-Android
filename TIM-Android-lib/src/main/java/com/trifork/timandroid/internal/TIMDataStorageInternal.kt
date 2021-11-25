package com.trifork.timandroid.internal

import com.trifork.timandroid.helpers.JWT
import com.trifork.timandroid.TIMDataStorage
import com.trifork.timandroid.models.errors.TIMError
import com.trifork.timencryptedstorage.StorageKey
import com.trifork.timencryptedstorage.TIMEncryptedStorage
import com.trifork.timencryptedstorage.models.TIMResult
import com.trifork.timencryptedstorage.models.errors.TIMSecureStorageError
import com.trifork.timencryptedstorage.models.toTIMSucces
import kotlinx.coroutines.CoroutineScope

internal sealed class TIMDataStorageKey {
    class KeyId(val userId: String) : TIMDataStorageKey()
    class RefreshToken(val userId: String) : TIMDataStorageKey()
    object AvailableUserIds : TIMDataStorageKey()

    val storageKey: StorageKey
        get() = when (this) {
            AvailableUserIds -> "availableUserIds"
            is KeyId -> "keyId_$userId"
            is RefreshToken -> "refreshToken_$userId"
        }

    companion object {
        fun getUserSpecificCases(userId: String) = listOf(
            KeyId(userId),
            RefreshToken(userId)
        )
    }
}

internal class TIMDataStorageInternal(
    private val encryptedStorage: TIMEncryptedStorage
) : TIMDataStorage {

    //region Available user ids
    override val availableUserIds: Set<String>
        get() {
            val result = get<Set<String>?>(TIMDataStorageKey.AvailableUserIds) {
                TODO("Figure out how to convert to a valid type for EncryptedStorage (Potentially Serialization)")
                null
            }
            return (result as? TIMResult.Success)?.value ?: emptySet()
        }

    private fun addAvailableUserId(userId: String) {
        val availableIds = availableUserIds.toMutableSet()
        availableIds.add(userId)
        store(
            TODO("Figure out how to convert to a valid type for EncryptedStorage (Potentially Serialization)"),
            TIMDataStorageKey.AvailableUserIds
        )
    }

    private fun removeAvailableUserId(userId: String) {
        val availableIds = availableUserIds.toMutableSet()
        availableIds.remove(userId)
        store(
            TODO("Figure out how to convert to a valid type for EncryptedStorage (Potentially Serialization)"),
            TIMDataStorageKey.AvailableUserIds
        )
    }
    //endregion


    override fun hasRefreshToken(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasBiometricAccessForRefreshToken(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun disableBiometricAccessForRefreshToken(userId: String) {
        TODO("Not yet implemented")
    }

    override fun getStoredRefreshToken(userId: String, password: String): TIMResult<JWT, TIMError> {
        TODO("Not yet implemented")
    }

    override fun getStoredRefreshTokenViaBiometric(userId: String) {
        TODO("Not yet implemented")
    }

    //TODO(Figure out if we need TIMResult as return value here)
    override fun storeRefreshTokenWithExistingPassword(scope: CoroutineScope, refreshToken: JWT, password: String): TIMResult<Unit, TIMError> {
        TODO("Not yet implemented")
    }

    override fun storeRefreshTokenWithNewPassword(refreshToken: JWT, password: String) {
        TODO("Not yet implemented")
       // TIMDataStorageKey.RefreshToken(refreshToken)

     //   encryptedStorage.storeWithNewKey(scope, password, JWT)
    }

    override fun enableBiometricAccessForRefreshToken(password: String, userId: String) {
        TODO("Not yet implemented")
    }

    override fun storeRefreshTokenWithLongSecret(refreshToken: JWT, longSecret: String): TIMResult<Unit, TIMError> {
        TODO("Not yet implemented")
    }

    //region NON-SECURE private read/write helpers
    private fun <T> get(
        key: TIMDataStorageKey,
        convertCallback: (ByteArray) -> T
    ): TIMResult<T, TIMSecureStorageError> {
        val dataResult = encryptedStorage.secureStorage.get(key.storageKey)
        return when (dataResult) {
            is TIMResult.Success -> convertCallback(dataResult.value).toTIMSucces()
            is TIMResult.Failure -> dataResult
        }
    }

    private fun store(data: ByteArray, key: TIMDataStorageKey) {
        encryptedStorage.secureStorage.store(data, key.storageKey)
    }
    //endregion

    override fun clear(userId: String) {
        //TODO(disableCurrentBiometricAccess(userId))

        TIMDataStorageKey.getUserSpecificCases(userId).forEach {
            encryptedStorage.remove(it.storageKey)
        }

        removeAvailableUserId(userId)
    }

}