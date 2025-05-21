package ac.id.umn.myapplication

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signupWithEmailPassword(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun loginWithEmailPassword(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signInWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken,
            null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    private val storageRef = FirebaseStorage.getInstance().reference

    fun uploadProfilePicture(imageUri: Uri, onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(false, "User not logged in")
        val profilePicRef = storageRef.child("profile_pictures/$uid.jpg")

        profilePicRef.putFile(imageUri)
            .addOnSuccessListener {
                profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build()
                    auth.currentUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                onResult(true, null)
                            } else {
                                onResult(false, updateTask.exception?.message)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun deleteProfilePicture(onResult: (Boolean, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(false, "User not logged in")
        val profilePicRef = storageRef.child("profile_pictures/$uid.jpg")

        profilePicRef.delete()
            .addOnSuccessListener {
                updateProfilePicture(null, onResult)
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("Object does not exist") == true) {
                    updateProfilePicture(null, onResult)
                } else {
                    onResult(false, e.message)
                }
            }
    }

    private fun updateProfilePicture(uri: Uri?, onResult: (Boolean, String?) -> Unit) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { updateTask ->
                if (updateTask.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, updateTask.exception?.message)
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signOut() {
        auth.signOut()
    }
}