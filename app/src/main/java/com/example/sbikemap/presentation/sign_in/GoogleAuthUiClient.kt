//package com.example.sbikemap.presentation.sign_in
//
//import android.content.Context
//import android.util.Log
//import androidx.credentials.ClearCredentialStateRequest
//import androidx.credentials.CredentialManager
//import androidx.credentials.CustomCredential
//import androidx.credentials.GetCredentialRequest
//import com.example.sbikemap.R
//import com.google.android.libraries.identity.googleid.GetGoogleIdOption
//import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
//import com.google.firebase.Firebase
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//import com.google.firebase.auth.auth
//import kotlinx.coroutines.tasks.await
//import java.util.concurrent.CancellationException
//
//class GoogleAuthUiClient(
//    private val context: Context,
//    private val credentialManager: CredentialManager
//) {
//    private val auth: FirebaseAuth = Firebase.auth
////    private val credentialManager = CredentialManager.create(context)
//
//    /**
//     * SIGN-IN REQUEST (tương đương beginSignIn() cũ)
//     */
//    suspend fun signIn(): SignInResult {
//        return try {
//            val googleIdOption = GetGoogleIdOption.Builder()
//                .setFilterByAuthorizedAccounts(false)
//                .setServerClientId(context.getString(R.string.web_client_id))
//                .build()
//
//            val request = GetCredentialRequest.Builder()
//                .addCredentialOption(googleIdOption)
//                .build()
//
//            val result = credentialManager.getCredential(context, request)
//
//            val credential = result.credential
//
//            // -> Ép kiểu GoogleIdTokenCredential
//            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
//
//            // Convert sang UserData của bạn
//            val user = UserData(
//                userId = googleIdToken.id,
//                username = googleIdToken.displayName,
//                profilePictureUrl = googleIdToken.profilePictureUri?.toString()
//            )
//
//            SignInResult(
//                data = user,
//                errorMessage = null
//            )
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            SignInResult(
//                data = null,
//                errorMessage = e.message
//            )
//        }
//    }
//
//    /**
//     * EXCHANGE TOKEN → LOGIN FIREBASE
//     * (tương đương signInWithIntent() cũ)
//     */
//    suspend fun signInWithIntent(credential: androidx.credentials.Credential): SignInResult {
//        return try {
//            if (credential is CustomCredential &&
//                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
//            ) {
//                val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
//
//                val firebaseCredential =
//                    GoogleAuthProvider.getCredential(googleToken.idToken, null)
//
//                val user = auth.signInWithCredential(firebaseCredential).await().user
//
//                SignInResult(
//                    data = user?.run {
//                        UserData(
//                            userId = uid,
//                            username = displayName,
//                            profilePictureUrl = photoUrl?.toString()
//                        )
//                    },
//                    errorMessage = null
//                )
//            } else {
//                Log.w("GoogleAuth", "Credential không phải Google ID Token!")
//                SignInResult(null, "Invalid Google credential")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            if (e is CancellationException) throw e
//            SignInResult(null, e.message)
//        }
//    }
//
//    /**
//     * SIGN OUT
//     * (tương đương oneTapClient.signOut() + auth.signOut())
//     */
//    suspend fun signOut() {
//        try {
//            credentialManager.clearCredentialState(ClearCredentialStateRequest())
//            auth.signOut()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    /**
//     * CHECK SIGNED-IN USER
//     */
//    fun getSignedInUser(): UserData? =
//        auth.currentUser?.run {
//            UserData(
//                userId = uid,
//                username = displayName,
//                profilePictureUrl = photoUrl?.toString()
//            )
//        }
//
//    private fun buildSignInRequest(): GetCredentialRequest {
//        val googleIdOption = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(false) // giống behavior One Tap cũ
//            .setServerClientId(context.getString(R.string.web_client_id))
//            .build()
//
//        return GetCredentialRequest.Builder()
//            .addCredentialOption(googleIdOption)
//            .build()
//    }
//
//}
