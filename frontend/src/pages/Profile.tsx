import React, { useState, useEffect } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import { User, KeyRound, Loader2, AlertCircle, CheckCircle } from 'lucide-react'
import apiClient from '@/services/api'

export default function Profile() {
  const { user } = useAuth()
  const [bio, setBio] = useState('')
  const [avatarUrl, setAvatarUrl] = useState('')
  const [oldPassword, setOldPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')

  // State controls
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false)
  const [isChangingPassword, setIsChangingPassword] = useState(false)
  const [profileMsg, setProfileMsg] = useState('')
  const [profileError, setProfileError] = useState('')
  const [passwordMsg, setPasswordMsg] = useState('')
  const [passwordError, setPasswordError] = useState('')

  useEffect(() => {
    // Fetch initial profile info
    const fetchProfile = async () => {
      try {
        const res = await apiClient.get('/users/me')
        if (res.data.success) {
          // If we had a separate profile endpoint we'd retrieve bio
          // Let's call put profile with empty update to load active fields or default them
          const initRes = await apiClient.put('/users/profile', {})
          if (initRes.data.success) {
            setBio(initRes.data.data.bio || '')
            setAvatarUrl(initRes.data.data.avatarUrl || '')
          }
        }
      } catch (err) {
        console.error('Failed to load profile details:', err)
      }
    }
    fetchProfile()
  }, [])

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsUpdatingProfile(true)
    setProfileMsg('')
    setProfileError('')

    try {
      const res = await apiClient.put('/users/profile', { bio, avatarUrl })
      if (res.data.success) {
        setProfileMsg('Profile bio updated successfully!')
      } else {
        setProfileError(res.data.message || 'Failed to update profile')
      }
    } catch (err: any) {
      setProfileError(err.response?.data?.message || 'Error occurred while updating profile')
    } finally {
      setIsUpdatingProfile(false)
    }
  }

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!oldPassword || !newPassword) return

    setIsChangingPassword(true)
    setPasswordMsg('')
    setPasswordError('')

    try {
      const res = await apiClient.put('/users/password', { oldPassword, newPassword })
      if (res.data.success) {
        setPasswordMsg('Password changed successfully!')
        setOldPassword('')
        setNewPassword('')
      } else {
        setPasswordError(res.data.message || 'Failed to change password')
      }
    } catch (err: any) {
      setPasswordError(err.response?.data?.message || 'Invalid old password or connection error')
    } finally {
      setIsChangingPassword(false)
    }
  }

  return (
    <div className="app-page max-w-5xl space-y-7">
      <div>
        <p className="eyebrow mb-2">Account</p>
        <h1 className="text-3xl font-semibold tracking-[-.04em]">
          Profile and security
        </h1>
        <p className="text-muted-foreground mt-1">
          Manage your personal details, workspace configurations, and account credentials
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Profile Card */}
        <form onSubmit={handleUpdateProfile} className="surface p-6 space-y-4">
          <h2 className="text-lg font-bold flex items-center gap-2 border-b border-border pb-3">
            <User className="h-5 w-5 text-primary" />
            Personal Bio Details
          </h2>

          {profileError && (
            <div className="rounded-lg border border-red-500/20 bg-red-500/5 p-3 flex items-start gap-2 text-xs text-red-400">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{profileError}</span>
            </div>
          )}

          {profileMsg && (
            <div className="rounded-lg border border-emerald-500/20 bg-emerald-500/5 p-3 flex items-start gap-2 text-xs text-emerald-400">
              <CheckCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{profileMsg}</span>
            </div>
          )}

          <div className="space-y-3">
            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-1 block">Username</label>
              <input
                type="text"
                disabled
                value={user?.username || ''}
                className="w-full px-3 py-2 rounded-lg border border-border bg-muted/30 text-sm text-muted-foreground outline-none cursor-not-allowed"
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-1 block">Email Address</label>
              <input
                type="email"
                disabled
                value={user?.email || ''}
                className="w-full px-3 py-2 rounded-lg border border-border bg-muted/30 text-sm text-muted-foreground outline-none cursor-not-allowed"
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-1 block">Bio Description</label>
              <textarea
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                placeholder="Tell us about yourself..."
                rows={4}
                className="w-full px-3 py-2 rounded-lg border border-input bg-muted/10 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all resize-none"
              />
            </div>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={isUpdatingProfile}
              className="inline-flex justify-center items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-semibold text-primary-foreground shadow-sm hover:opacity-95 active:scale-[.99] transition-all disabled:opacity-50"
            >
              {isUpdatingProfile ? (
                <Loader2 className="h-3.5 w-3.5 animate-spin" />
              ) : (
                'Save Profile Bio'
              )}
            </button>
          </div>
        </form>

        {/* Change Password Card */}
        <form onSubmit={handleChangePassword} className="surface p-6 space-y-4">
          <h2 className="text-lg font-bold flex items-center gap-2 border-b border-border pb-3">
            <KeyRound className="h-5 w-5 text-primary" />
            Security Configuration
          </h2>

          {passwordError && (
            <div className="rounded-lg border border-red-500/20 bg-red-500/5 p-3 flex items-start gap-2 text-xs text-red-400">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{passwordError}</span>
            </div>
          )}

          {passwordMsg && (
            <div className="rounded-lg border border-emerald-500/20 bg-emerald-500/5 p-3 flex items-start gap-2 text-xs text-emerald-400">
              <CheckCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{passwordMsg}</span>
            </div>
          )}

          <div className="space-y-3">
            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-1 block">Current Password</label>
              <input
                type="password"
                required
                value={oldPassword}
                onChange={(e) => setOldPassword(e.target.value)}
                placeholder="Enter current password"
                className="w-full px-3 py-2 rounded-lg border border-input bg-muted/10 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all"
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-muted-foreground mb-1 block">New Password</label>
              <input
                type="password"
                required
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Enter new password (min 6 chars)"
                className="w-full px-3 py-2 rounded-lg border border-input bg-muted/10 text-sm focus:outline-none focus:ring-1 focus:ring-primary focus:border-primary transition-all"
              />
            </div>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={isChangingPassword || !oldPassword || !newPassword}
              className="inline-flex justify-center items-center gap-2 rounded-xl bg-primary px-4 py-2.5 text-xs font-semibold text-primary-foreground shadow-sm hover:opacity-95 active:scale-[.99] transition-all disabled:opacity-50"
            >
              {isChangingPassword ? (
                <Loader2 className="h-3.5 w-3.5 animate-spin" />
              ) : (
                'Change Password'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
