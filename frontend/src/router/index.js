import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import PublicRegistrationView from '../views/PublicRegistrationView.vue'
import ForgotPasswordView from '../views/ForgotPasswordView.vue'
import VerifyCodeView from '../views/VerifyCodeView.vue'
import ResetPasswordView from '../views/ResetPasswordView.vue'
import TemporaryPasswordView from '../views/TemporaryPasswordView.vue'
import DashboardView from '../views/DashboardView.vue'
import UsersView from '../views/UsersView.vue'
import RegisterUserView from '../views/RegisterUserView.vue'
import RolesView from '../views/RolesView.vue'
import AuditView from '../views/AuditView.vue'
import DeniedView from '../views/DeniedView.vue'
import NotFoundView from '../views/NotFoundView.vue'

const routes = [
  { path: '/login', component: LoginView, meta: { public: true } }, { path: '/register', component: PublicRegistrationView, meta: { public: true } }, { path: '/recover', component: ForgotPasswordView, meta: { public: true } },
  { path: '/verify-code', component: VerifyCodeView, meta: { public: true } }, { path: '/reset-password', component: ResetPasswordView, meta: { public: true } },
  { path: '/change-password', component: TemporaryPasswordView, meta: { auth: true, passwordOnly: true } },
  { path: '/', component: DashboardView, meta: { auth: true } },
  { path: '/register-user', component: RegisterUserView, meta: { auth: true, permission: 'USERS_CREATE' } },
  { path: '/users', component: UsersView, meta: { auth: true, permission: 'USERS_VIEW' } }, { path: '/roles', component: RolesView, meta: { auth: true, permission: 'PERMISSIONS_MANAGE' } },
  { path: '/audit', component: AuditView, meta: { auth: true, permission: 'AUDIT_VIEW' } }, { path: '/denied', component: DeniedView, meta: { auth: true } }, { path: '/:pathMatch(.*)*', component: NotFoundView, meta: { public: true } },
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach(async (to) => {
  const auth = useAuthStore(); await auth.bootstrap()
  if (to.meta.public && auth.authenticated && !auth.user.requiresPasswordChange && !['/verify-code', '/reset-password'].includes(to.path)) return '/'
  if (to.meta.auth && !auth.authenticated) return { path: '/login', query: { redirect: to.fullPath } }
  if (auth.authenticated && auth.user.requiresPasswordChange && !to.meta.passwordOnly) return '/change-password'
  if (to.meta.passwordOnly && auth.authenticated && !auth.user.requiresPasswordChange) return '/'
  if (to.meta.permission && !auth.hasPermission(to.meta.permission)) return '/denied'
})
export default router
