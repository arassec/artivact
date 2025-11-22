<template>
  <ArtivactContent>
    <div class="col">
      <h1 class="av-text-h1">{{ $t("AccountsConfigurationPage.heading") }}</h1>
      <div class="q-mb-lg">
        {{ $t("AccountsConfigurationPage.description") }}
      </div>
      <q-card>
        <q-card-section class="bg-primary text-white">
          <div class="text-h6">{{ $t("AccountsConfigurationPage.card.heading") }}</div>
        </q-card-section>

        <q-list class="rounded-borders q-mb-lg" v-if="accountsRef">
          <template v-for="(account, index) of accountsRef" :key="index">
            <q-item>
              <q-item-section class="account-label">
                {{ account.username }}
                {{ account.email ? "(" + account.email + ")" : "" }}
              </q-item-section>

              <div class="float-right">
                <q-btn
                  round
                  dense
                  flat
                  icon="edit"
                  size="md"
                  @click="
                    editAccountRef = account;
                    showEditModalRef = true;
                  ">
                  <q-tooltip>{{ $t("AccountsConfigurationPage.card.button.tooltip.edit") }}</q-tooltip>
                </q-btn>
                <q-btn
                  round
                  dense
                  flat
                  icon="delete"
                  size="md"
                  @click="
                    deleteAccountIdRef = account.id;
                    showDeleteModalRef = true;
                  ">
                  <q-tooltip>{{ $t("AccountsConfigurationPage.card.button.tooltip.delete") }}</q-tooltip>
                </q-btn>
              </div>
            </q-item>
            <q-separator></q-separator>
          </template>
        </q-list>
      </q-card>

      <q-btn
        :label="$t('AccountsConfigurationPage.button.addAccount')"
        @click="openCreateAccountModal()"
        color="primary"
        class="float-right q-mb-lg"
      />
    </div>

    <artivact-dialog :dialog-model="showCreateModalRef">
      <template v-slot:header>
        {{ $t("AccountsConfigurationPage.dialog.create.heading") }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <q-input
            outlined
            v-model="usernameRef"
            class="col-5 q-mb-lg"
            type="text"
            name="username"
            :label="$t('Common.username')"
          />

          <q-input
            outlined
            v-model="passwordRef"
            class="col-5"
            type="password"
            name="password"
            :label="$t('Common.password')"
            :error="passwordValidationFailedRef"
          />

          <q-input
            outlined
            v-model="passwordRepeatRef"
            class="col-5"
            type="password"
            name="password"
            :label="$t('Common.passwordRepeat')"
            :error="passwordValidationFailedRef"
          />

          <q-input
            outlined
            v-model="emailRef"
            class="col-5 q-mb-md"
            type="text"
            name="email"
            :label="$t('Common.email')"
          />

          <div>
            <q-checkbox
              v-model="userRef"
              class="col-5"
              name="user"
              :label="$t('AccountsConfigurationPage.dialog.user')"
            />
          </div>

          <div>
            <q-checkbox
              v-model="adminRef"
              class="col-5"
              name="admin"
              :label="$t('AccountsConfigurationPage.dialog.admin')"
            />
          </div>
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showCreateModalRef = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          :label="$t('Common.save')"
          color="primary"
          @click="showCreateModalRef = !createAccount()"
        />
      </template>
    </artivact-dialog>

    <artivact-dialog :dialog-model="showEditModalRef">
      <template v-slot:header>
        {{ $t('AccountsConfigurationPage.dialog.edit.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          <q-input
            outlined
            v-model="editAccountRef.username"
            class="col-5 q-mb-lg"
            type="text"
            name="username"
            :label="$t('Common.username')"
          />

          <q-input
            outlined
            v-model="editAccountRef.password"
            class="col-5"
            type="password"
            name="password"
            :label="$t('Common.password')"
            :error="passwordValidationFailedRef"
          />

          <q-input
            outlined
            v-model="passwordRepeatRef"
            class="col-5"
            type="password"
            name="password"
            :label="$t('Common.passwordRepeat')"
            :error="passwordValidationFailedRef"
          />

          <q-input
            outlined
            v-model="editAccountRef.email"
            class="col-5 q-mb-md"
            type="text"
            name="email"
            :label="$t('Common.email')"
          />

          <div>
            <q-checkbox
              v-model="editAccountRef.user"
              class="col-5"
              name="user"
              :label="$t('AccountsConfigurationPage.dialog.user')"
            />
          </div>

          <div>
            <q-checkbox
              v-model="editAccountRef.admin"
              class="col-5"
              name="admin"
              :label="$t('AccountsConfigurationPage.dialog.admin')"
            />
          </div>
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn
          :label="$t('Common.cancel')"
          color="primary"
          @click="showEditModalRef = false"
        />
      </template>

      <template v-slot:approve>
        <q-btn
          :label="$t('Common.save')"
          color="primary"
          @click="showEditModalRef = !updateAccount()"
        />
      </template>
    </artivact-dialog>

    <artivact-dialog :dialog-model="showDeleteModalRef" :warn="true">
      <template v-slot:header>
        {{ $t('AccountsConfigurationPage.dialog.delete.heading') }}
      </template>

      <template v-slot:body>
        <q-card-section>
          {{ $t('AccountsConfigurationPage.dialog.delete.description') }}
        </q-card-section>
      </template>

      <template v-slot:cancel>
        <q-btn :label="$t('Common.cancel')" color="primary" @click="showDeleteModalRef = false"/>
      </template>

      <template v-slot:approve>
        <q-btn
          :label="$t('AccountsConfigurationPage.dialog.delete.button')"
          color="primary"
          @click="deleteAccount(deleteAccountIdRef)"
        />
      </template>
    </artivact-dialog>

  </ArtivactContent>
</template>

<script setup lang="ts">
import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import {onMounted, Ref, ref} from 'vue';
import {Account} from 'components/artivact-models';
import ArtivactContent from 'components/ArtivactContent.vue';
import ArtivactDialog from 'components/ArtivactDialog.vue';
import {useI18n} from 'vue-i18n';

const quasar = useQuasar();
const i18n = useI18n();

const showCreateModalRef = ref(false);
const showEditModalRef = ref(false);
const showDeleteModalRef = ref(false);

const usernameRef = ref('');
const passwordRef = ref('');
const passwordRepeatRef = ref('');
const emailRef = ref('');
const userRef = ref(true);
const adminRef = ref(false);

const passwordValidationFailedRef = ref(false);

const deleteAccountIdRef = ref(-1);
const editAccountRef: Ref<Account | null> = ref(null);

const accountsRef: Ref<Account[] | null> = ref(null);

function loadAccounts() {
  api
    .get('/api/account')
    .then((response) => {
      accountsRef.value = response.data;
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.loading.failed', {item: i18n.t('Common.items.accounts')}),
        icon: 'report_problem',
      });
    });
}

function openCreateAccountModal() {
  usernameRef.value = '';
  passwordRef.value = '';
  passwordRepeatRef.value = '';
  emailRef.value = '';
  userRef.value = true;
  adminRef.value = false;

  showCreateModalRef.value = true;
}

function createAccount(): boolean {
  if (!passwordRef.value || passwordRef.value !== passwordRepeatRef.value) {
    passwordValidationFailedRef.value = true;
    return false;
  } else {
    passwordValidationFailedRef.value = false;
    passwordRepeatRef.value = '';
  }

  let account: Account = {
    id: undefined,
    version: 0,
    username: usernameRef.value,
    password: passwordRef.value,
    email: emailRef.value,
    user: userRef.value,
    admin: adminRef.value
  };

  api
    .post('/api/account', account)
    .then((response) => {
      accountsRef.value?.push(response.data);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.creating.success', {item: i18n.t('Common.items.account')}),
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.creating.failure', {item: i18n.t('Common.items.account')}),
        icon: 'report_problem',
      });
    });

  return true;
}

function updateAccount(): boolean {
  if (!editAccountRef.value) {
    return false;
  }
  if (
    editAccountRef.value.password != null &&
    editAccountRef.value.password !== passwordRepeatRef.value
  ) {
    passwordValidationFailedRef.value = true;
    return false;
  } else {
    passwordValidationFailedRef.value = false;
    passwordRepeatRef.value = '';
  }

  api
    .put('/api/account', editAccountRef.value)
    .then(() => {
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.success', {item: i18n.t('Common.items.account')}),
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.saving.failure', {item: i18n.t('Common.items.account')}),
        icon: 'report_problem',
      });
    });

  return true;
}

function deleteAccount(id: number) {
  showDeleteModalRef.value = false;
  api
    .delete('/api/account/' + id)
    .then(() => {
      loadAccounts();
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.success', {item: i18n.t('Common.items.account')}),
        icon: 'check',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: i18n.t('Common.messages.deleting.failed', {item: i18n.t('Common.items.account')}),
        icon: 'report_problem',
      });
    });
}

onMounted(() => {
  loadAccounts();
});
</script>

<style scoped>
.account-label {
  font-size: large;
}
</style>
