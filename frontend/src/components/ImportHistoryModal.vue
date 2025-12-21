<script setup>
import {ref, onMounted} from 'vue';
import {api} from '../services/api';

const props = defineProps({
  currentUser: {type: String, default: 'anonymous'},
  currentRole: {type: String, default: 'USER'}
});
const emit = defineEmits(['close']);

const operations = ref([]);
const loading = ref(false);
const loadError = ref('');

async function fetchHistory() {
  loading.value = true;
  loadError.value = '';
  try {
    const res = await api.get('/imports', {
      headers: {
        'X-User': props.currentUser,
        'X-Role': props.currentRole
      }
    });
    operations.value = res.data || [];
  } catch (err) {
    loadError.value = err.response?.data?.message || err.message;
    operations.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(fetchHistory);

function close() {
  emit('close');
}

function formatDate(d) {
  if (!d) return '';
  try {
    return new Date(d).toLocaleString();
  } catch {
    return d;
  }
}
</script>

<template>
  <div class="modal-backdrop" @click.self="close">
    <div class="modal">
      <h2>Import History</h2>

      <div class="controls">
        <button @click="fetchHistory" :disabled="loading">Refresh</button>
        <div class="spacer"></div>
      </div>

      <div v-if="loadError" class="error-block">
        <strong>Error:</strong> {{ loadError }}
      </div>

      <table class="history-table" v-if="operations.length">
        <thead>
        <tr>
          <th>id</th>
          <th>status</th>
          <th>user</th>
          <th>imported</th>
          <th>error</th>
          <th>createdAt</th>
          <th>finishedAt</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="op in operations" :key="op.id">
          <td>{{ op.id }}</td>
          <td>{{ op.status }}</td>
          <td>{{ op.username }}</td>
          <td>{{ op.importedCount ?? '—' }}</td>
          <td class="err-cell">{{ op.errorMessage ?? '—' }}</td>
          <td>{{ formatDate(op.createdAt) }}</td>
          <td>{{ formatDate(op.finishedAt) }}</td>
        </tr>
        </tbody>
      </table>

      <div v-else class="empty">
        <em v-if="!loading">No operations found.</em>
        <em v-else>Loading…</em>
      </div>

      <div class="modal-actions">
        <button class="neutral" @click="close">Close</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 50;
}

.modal {
  width: 820px;
  background: #fff;
  padding: 1.1rem;
  border-radius: 8px;
  max-height: 85vh;
  overflow: auto;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.18);
}

h2 {
  margin: 0 0 0.6rem 0;
}

.controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.6rem;
}

.controls .spacer {
  flex: 1;
}

button {
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 0.6rem;
}

.history-table th, .history-table td {
  border: 1px solid #e0e0e0;
  padding: 0.4rem 0.6rem;
  text-align: left;
  font-size: 0.9rem;
}

.history-table thead {
  background: #fafafa;
}

.error-block {
  background: #fff5f5;
  color: #b71c1c;
  padding: 0.5rem;
  border-radius: 6px;
  margin-bottom: 0.6rem;
  border: 1px solid #f2dede;
}

.empty {
  padding: 0.6rem;
  color: #666;
}

.err-cell {
  color: #b71c1c;
  white-space: pre-wrap;
  max-width: 260px;
  word-break: break-word;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.6rem;
}

.neutral {
  background: #e0e0e0;
  color: #111;
}
</style>
