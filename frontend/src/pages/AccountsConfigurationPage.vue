<template>
  <ArtivactContent>
    <div class="col">
      <h1 class="av-text-h1">Accounts Administration</h1>
      <div class="q-mb-lg">
        All system accounts can be configured on this page.
      </div>

      <q-card>
        <q-card-section class="bg-primary text-white">
          <div class="text-h6">Accounts</div>
        </q-card-section>

        <q-list class="rounded-borders q-mb-lg" v-if="accountsRef">
          <template v-for="(account, index) of accountsRef" :key="index">
            <q-item>
              <q-item-section class="account-label">
                {{ account.username }}
                {{ account.email !== "" ? "(" + account.email + ")" : "" }}
              </q-item-section>

              <q-item-section side>
                <q-btn
                  round
                  dense
                  flat
                  class="float-right"
                  color="white"
                  icon="delete"
                  size="md"
                  @click="deleteAccount(index)"
                ></q-btn>
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
                  "
                ></q-btn>
                <q-btn
                  round
                  dense
                  flat
                  icon="delete"
                  size="md"
                  @click="
                    deleteAccountIdRef = account.id;
                    showDeleteModalRef = true;
                  "
                ></q-btn>
              </div>
            </q-item>
            <q-separator></q-separator>
          </template>
        </q-list>
      </q-card>

      <q-btn
        label="Add Account"
        @click="openCreateAccountModal()"
        color="primary"
        class="float-right q-mb-lg"
      />
    </div>

    <q-dialog v-model="showCreateModalRef" persistent>
      <q-card class="q-mb-lg artivact-modal-content">
        <q-card-section class="bg-primary text-white">
          <div class="text-h6">Create Account</div>
        </q-card-section>

        <q-card-section>
          <q-input
            outlined
            v-model="usernameRef"
            class="col-5 q-mb-lg"
            type="text"
            name="username"
            label="Username"
          />

          <q-input
            outlined
            v-model="passwordRef"
            class="col-5"
            type="password"
            name="password"
            label="Password"
            :error="passwordValidationFailedRef"
          />

          <q-input
            outlined
            v-model="passwordRepeatRef"
            class="col-5"
            type="password"
            name="password"
            label="Password (repeat)"
            :error="passwordValidationFailedRef"
          />

          <q-input
            outlined
            v-model="emailRef"
            class="col-5 q-mb-md"
            type="text"
            name="email"
            label="E-Mail"
          />

          <div>
            <q-checkbox
              v-model="userRef"
              class="col-5"
              name="user"
              label="User?"
            />
          </div>

          <div>
            <q-checkbox
              v-model="adminRef"
              class="col-5"
              name="admin"
              label="Admin?"
            />
          </div>
        </q-card-section>

        <q-card-section>
          <q-btn
            label="Cancel"
            color="primary"
            @click="showCreateModalRef = false"
          />
          <q-btn
            label="Save"
            color="primary"
            class="float-right"
            @click="showCreateModalRef = !createAccount()"
          />
        </q-card-section>
      </q-card>
    </q-dialog>

    <q-dialog
      v-model="showEditModalRef"
      persistent
      v-if="editAccountRef"
      class="artivact-modal"
    >
      <q-card class="q-mb-lg artivact-modal-content">
        <q-card-section class="bg-primary text-white">
          <div class="text-h6">Edit Account</div>
        </q-card-section>

        <q-card-section>
          <q-input
            outlined
            v-model="editAccountRef.username"
            class="col-5 q-mb-lg"
            type="text"
            name="username"
            label="Username"
          />

          <q-input
            outlined
            v-model="editAccountRef.password"
            class="col-5"
            type="password"
            name="password"
            label="Password"
            :error="passwordValidationFailedRef"
          />

          <q-input
            outlined
            v-model="passwordRepeatRef"
            class="col-5"
            type="password"
            name="password"
            label="Password (repeat)"
            :error="passwordValidationFailedRef"
          />

          <q-input
            outlined
            v-model="editAccountRef.email"
            class="col-5 q-mb-md"
            type="text"
            name="email"
            label="E-Mail"
          />

          <div>
            <q-checkbox
              v-model="editAccountRef.user"
              class="col-5"
              name="user"
              label="User?"
            />
          </div>

          <div>
            <q-checkbox
              v-model="editAccountRef.admin"
              class="col-5"
              name="admin"
              label="Admin?"
            />
          </div>
        </q-card-section>

        <q-card-section>
          <q-btn
            label="Cancel"
            color="primary"
            @click="showEditModalRef = false"
          />
          <q-btn
            label="Save"
            color="primary"
            class="float-right"
            @click="showEditModalRef = !updateAccount()"
          />
        </q-card-section>
      </q-card>
    </q-dialog>

    <q-dialog v-model="showDeleteModalRef" persistent class="artivact-modal">
      <q-card class="artivact-modal-content">
        <q-card-section class="row items-center">
          <q-icon
            name="warning"
            size="md"
            color="warning"
            class="q-mr-md"
          ></q-icon>
          <h3 class="av-text-h3">Delete Account?</h3>
          <div class="q-ml-sm">
            Are you sure you want to delete this account? This action cannot be
            undone!
          </div>
        </q-card-section>

        <q-card-section>
          <q-btn flat label="Cancel" color="primary" v-close-popup />
          <q-btn
            flat
            label="Delete Account"
            color="primary"
            v-close-popup
            @click="deleteAccount(deleteAccountIdRef)"
            class="float-right"
          />
        </q-card-section>
      </q-card>
    </q-dialog>
  </ArtivactContent>
</template>

<script setup lang="ts">
import {useQuasar} from 'quasar';
import {api} from 'boot/axios';
import {onMounted, Ref, ref} from 'vue';
import {Account} from 'components/models';
import ArtivactContent from 'components/ArtivactContent.vue';

const quasar = useQuasar();

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
        message: 'Loading accounts failed',
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
    admin: adminRef.value,
  };

  api
    .post('/api/account', account)
    .then((response) => {
      accountsRef.value?.push(response.data);
      quasar.notify({
        color: 'positive',
        position: 'bottom',
        message: 'Account created',
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading accounts failed',
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
        message: 'Account saved',
        icon: 'done',
      });
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Loading accounts failed',
        icon: 'report_problem',
      });
    });

  return true;
}

function deleteAccount(id: number) {
  api
    .delete('/api/account/' + id)
    .then(() => {
      loadAccounts();
    })
    .catch(() => {
      quasar.notify({
        color: 'negative',
        position: 'bottom',
        message: 'Deleting failed',
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
