<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {api} from '../services/api';
import CreateEditDialog from '../components/CreateEditDialog.vue';
import GroupViewModal from "../components/GroupViewModal.vue";
import SpecialOpsModal from '../components/SpecialOpsModal.vue'
import {createWebSocket} from '../services/websocket';
import ImportUploadModal from '../components/ImportUploadModal.vue';
import ImportHistoryModal from '../components/ImportHistoryModal.vue';

const showImportModal = ref(false);
const showImportHistory = ref(false);

const currentUser = ref(localStorage.getItem('app.user') || 'alice');
const currentRole = ref(localStorage.getItem('app.role') || 'ADMIN');

const groups = ref([]);
const page = ref(0);
const pageSize = 10;
const sortField = ref('id');
const sortAsc = ref(true);
const showDialog = ref(false);
const selectedGroup = ref(null);

const showViewDialog = ref(false);
const viewGroup = ref(null);
const totalPages = ref(1);

const popupVisible = ref(false);
const popupCol = ref(null);
const popupX = ref(0);
const popupY = ref(0);

const showSpecial = ref(false);

const filters = ref({
  id: '',
  name: '',
  studentsCount: '',
  expelledStudents: '',
  transferredStudents: '',
  shouldBeExpelled: '',
  averageMark: '',
  formOfEducation: '',
  semesterEnum: '',
  groupAdmin: ''
});

const activeFilter = ref(null);

const columns = ['id', 'name', 'studentsCount', 'expelledStudents', 'transferredStudents', 'shouldBeExpelled', 'averageMark', 'formOfEducation', 'semesterEnum', 'groupAdmin'];

let stompClient = null;
let debounceTimer = null;

async function fetchGroups() {
  try {
    const res = await api.get('/groups', {
      params: {
        page: page.value,
        size: pageSize,
        sort: sortField.value,
        asc: sortAsc.value
      }
    });
    const payload = res.data;
    if (payload && Array.isArray(payload.content)) {
      groups.value = payload.content;
      totalPages.value = payload.totalPages ?? 1;
      if (typeof payload.number === 'number') page.value = payload.number;
    } else if (Array.isArray(payload)) {
      groups.value = payload;
      totalPages.value = Math.ceil(groups.value.length / pageSize) || 1;
    } else {
      groups.value = payload.content ?? [];
      totalPages.value = payload.totalPages ?? 1;
    }
  } catch (err) {
    console.error('Failed to fetch groups', err);
  }
}

function sortBy(field) {
  if (sortField.value === field) sortAsc.value = !sortAsc.value;
  else {
    sortField.value = field;
    sortAsc.value = true;
  }
  fetchGroups();
}

function nextPage() {
  if (page.value + 1 < totalPages.value) {
    page.value++;
    fetchGroups();
  }
}

function prevPage() {
  if (page.value > 0) {
    page.value--;
    fetchGroups();
  }
}

function editGroup(group) {
  selectedGroup.value = {...group};
  showDialog.value = true;
}

function openCreateDialog() {
  selectedGroup.value = null;
  showDialog.value = true;
}

function closeDialog() {
  showDialog.value = false;
}

function viewGroupDetails(group) {
  viewGroup.value = group;
  showViewDialog.value = true;
}

async function deleteGroup(id) {
  try {
    await api.delete(`/groups/${id}`);
    await fetchGroups();
  } catch (err) {
    console.error('Delete failed', err);
    alert('Delete failed: ' + (err.response?.data?.message ?? err.message));
  }
}

function handleWsMessage(payload) {
  if (!payload || !payload.event) return;
  if (['created', 'updated', 'deleted'].includes(payload.event)) {
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      fetchGroups();
      debounceTimer = null;
    }, 250);
  }
}

onMounted(() => {
  fetchGroups();
  stompClient = createWebSocket(handleWsMessage);
});

onUnmounted(() => {
  if (stompClient?.deactivate) stompClient.deactivate();
});

const filteredGroups = computed(() => {
  return groups.value.filter(g => {
    return Object.entries(filters.value).every(([col, val]) => {
      if (!val) return true;
      let field = g[col];
      if (col === 'groupAdmin') field = g.groupAdmin?.name || '';
      if (field === null || field === undefined) field = '';
      return String(field).toLowerCase().includes(val.toLowerCase());
    });
  });
});

function onSaved() {
  showDialog.value = false;
  fetchGroups();
}

function toggleFilter(col, event) {
  if (popupVisible.value && popupCol.value === col) {
    popupVisible.value = false;
    popupCol.value = null;
  } else {
    popupCol.value = col;
    const rect = event.target.getBoundingClientRect();
    popupX.value = rect.right + window.scrollX;
    popupY.value = rect.bottom + window.scrollY;
    popupVisible.value = true;
  }
}
</script>

<template>
  <div class="container">
    <h1>Study Groups</h1>

    <div
        v-if="popupVisible"
        class="filter-popup"
        :style="{ top: popupY + 'px', left: popupX + 'px' }"
    >
      <input
          v-model="filters[popupCol]"
          placeholder="Type to filter..."
          class="filter-input"
      />
    </div>

    <table class="group-table">
      <thead>
      <tr>
        <th v-for="col in columns" :key="col" @click="sortBy(col)">
          {{ col }}
          <button
              class="filter-btn"
              @click.stop="toggleFilter(col, $event)"
          >🔍
          </button>
        </th>
        <th>Actions</th>
      </tr>
      <tr v-if="activeFilter">
        <th v-for="col in columns" :key="col">
          <input v-if="activeFilter === col"
                 v-model="filters[col]"
                 placeholder="Type to filter..."
                 class="filter-input">
        </th>
        <th></th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="group in filteredGroups" :key="group.id">
        <td>{{ group.id }}</td>
        <td>{{ group.name }}</td>
        <td>{{ group.studentsCount }}</td>
        <td>{{ group.expelledStudents }}</td>
        <td>{{ group.transferredStudents }}</td>
        <td>{{ group.shouldBeExpelled }}</td>
        <td>{{ group.averageMark }}</td>
        <td>{{ group.formOfEducation || '—' }}</td>
        <td>{{ group.semesterEnum }}</td>
        <td>{{ group.groupAdmin?.name || '—' }}</td>
        <td>
          <div class="action-buttons">
            <button class="view-btn" @click="viewGroupDetails(group)">View</button>
            <button class="edit-btn" @click="editGroup(group)">Edit</button>
            <button class="delete-btn" @click="deleteGroup(group.id)">Delete</button>
          </div>
        </td>
      </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button @click="prevPage" :disabled="page===0">Prev</button>
      <span>Page {{ page + 1 }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="page + 1 >= totalPages">Next</button>
    </div>

    <button class="create-btn" @click="openCreateDialog">Create New Group</button>
    <button class="special-btn" @click="showSpecial = true">Special Ops</button>
    <button class="import-btn" @click="showImportModal = true">Import</button>
    <button class="history-btn" @click="showImportHistory = true">Import History</button>

    <CreateEditDialog
        v-if="showDialog"
        :group="selectedGroup"
        @close="closeDialog"
        @saved="onSaved"
    />

    <GroupViewModal
        v-if="showViewDialog"
        :group="viewGroup"
        @close="showViewDialog = false"
    />

    <SpecialOpsModal
        v-if="showSpecial"
        :groups="groups"
        @close="showSpecial = false"
        @done="fetchGroups(); showSpecial=false"
    />

    <ImportUploadModal
        v-if="showImportModal"
        :currentUser="currentUser"
        :currentRole="currentRole"
        @close="showImportModal = false"
        @done="fetchGroups(); showImportModal = false"
    />

    <ImportHistoryModal
        v-if="showImportHistory"
        :currentUser="currentUser"
        :currentRole="currentRole"
        @close="showImportHistory = false"
    />

  </div>
</template>

<style scoped>
.container {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  padding: 1rem;
  max-width: 1200px;
}

h1 {
  margin-bottom: 1rem;
}

.group-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
  background-color: #fff;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  font-size: 0.9rem;
}

.group-table th, .group-table td {
  padding: 0.5rem 0.75rem;
  border: 1px solid #ddd;
  text-align: center;
}

.group-table thead th {
  background-color: #f0f0f0;
  cursor: pointer;
  position: relative;
}

.group-table tbody tr:hover {
  background-color: #f5faff;
}

.filter-btn {
  margin-left: 0.3rem;
  background: none;
  border: none;
  cursor: pointer;
}

.filter-popup {
  position: absolute;
  background: white;
  border: 1px solid #ccc;
  padding: 0.3rem;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.filter-input {
  width: 100%;
  padding: 0.25rem;
  font-size: 0.85rem;
  border: 1px solid #aaa;
  border-radius: 3px;
}

.action-buttons {
  display: flex;
  gap: 0.25rem;
  justify-content: center;
}

button {
  border: none;
  border-radius: 4px;
  padding: 0.35rem 0.75rem;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.view-btn {
  background-color: #9c27b0;
  color: white;
}

.view-btn:hover {
  background-color: #7b1fa2;
}

.edit-btn {
  background-color: #2196F3;
  color: white;
}

.edit-btn:hover {
  background-color: #1976d2;
}

.delete-btn {
  background-color: #f44336;
  color: white;
}

.delete-btn:hover {
  background-color: #d32f2f;
}

.create-btn {
  background-color: #4CAF50;
  color: white;
  margin-top: 0.5rem;
}

.create-btn:hover {
  background-color: #45a049;
}

.special-btn {
  background: #ff9800;
  color: white;
  margin-left: 8px;
}

.special-btn:hover {
  background: #fb8c00;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.import-btn {
  background: #6a1b9a;
  color: white;
  margin-left: 8px;
}

.import-btn:hover {
  background: #5e35b1;
}

.history-btn {
  background: #455a64;
  color: white;
  margin-left: 8px;
}

.history-btn:hover {
  background: #37474f;
}
</style>
