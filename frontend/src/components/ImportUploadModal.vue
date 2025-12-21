<script setup>
import {ref} from 'vue';
import {api} from '../services/api';

const props = defineProps({
  currentUser: {type: String, default: 'anonymous'},
  currentRole: {type: String, default: 'USER'}
});
const emit = defineEmits(['close', 'done']);

const selectedFile = ref(null);
const uploadError = ref('');
const result = ref(null);
const uploading = ref(false);

function onFileChange(e) {
  uploadError.value = '';
  result.value = null;
  const f = e.target.files && e.target.files[0];
  selectedFile.value = f || null;
}

async function upload() {
  uploadError.value = '';
  result.value = null;

  if (!selectedFile.value) {
    uploadError.value = 'No file selected';
    return;
  }

  const fd = new FormData();
  fd.append('file', selectedFile.value);

  const localUser = (() => {
    try {
      return localStorage.getItem('app.user');
    } catch {
      return null;
    }
  })();
  const localRole = (() => {
    try {
      return localStorage.getItem('app.role');
    } catch {
      return null;
    }
  })();

  const headerUser = localUser && localUser.trim() !== '' ? localUser : (props.currentUser || 'anonymous');
  const headerRole = localRole && localRole.trim() !== '' ? localRole : (props.currentRole || 'USER');

  uploading.value = true;
  try {
    const res = await api.post('/groups/import', fd, {
      headers: {
        'X-User': headerUser,
        'X-Role': headerRole
      },
      timeout: 120000
    });

    result.value = res.data;
    emit('done', result.value);
  } catch (err) {
    if (err.response?.data) {
      const data = err.response.data;

      if (Array.isArray(data.errors)) {
        uploadError.value = data.errors.map(e =>
            `#${(typeof e.index === 'number' && e.index >= 0) ? e.index : '?'} ${e.field}: ${e.message}`
        ).join('\n');
      } else if (Array.isArray(data)) {
        uploadError.value = data.map(e => {
          if (e && typeof e === 'object') return `#${e.index ?? '?'} ${e.field ?? 'file'}: ${e.message ?? JSON.stringify(e)}`;
          return String(e);
        }).join('\n');
      } else if (data.message) {
        uploadError.value = data.message;
      } else {
        try {
          uploadError.value = JSON.stringify(data);
        } catch {
          uploadError.value = String(data);
        }
      }
    } else {
      uploadError.value = err.message || 'Unknown error';
    }
  } finally {
    uploading.value = false;
  }
}

function close() {
  emit('close');
}
</script>

<template>
  <div class="modal-backdrop" @click.self="close">
    <div class="modal">
      <h2>Import Groups (JSON)</h2>

      <div class="section">
        <label class="file-label">Select JSON file</label>
        <input type="file" accept=".json,application/json" @change="onFileChange"/>
      </div>

      <div v-if="result" class="section result">
        <h4>Result</h4>
        <div>Imported: <strong>{{ result.imported }}</strong></div>
        <div v-if="result.message">Message: {{ result.message }}</div>
      </div>

      <div v-if="uploadError" class="section error-block">
        <h4>Error</h4>
        <pre class="error-text">{{ uploadError }}</pre>
      </div>

      <div class="modal-actions">
        <button class="primary" @click="upload" :disabled="uploading">
          {{ uploading ? 'Uploading…' : 'Upload' }}
        </button>
        <button class="neutral" @click="close" :disabled="uploading">Close</button>
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
  width: 520px;
  background: #fff;
  padding: 1.2rem;
  border-radius: 8px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.18);
  max-height: 85vh;
  overflow: auto;
}

h2 {
  margin: 0 0 0.75rem 0;
}

.section {
  margin: 0.6rem 0;
}

.file-label {
  display: block;
  margin-bottom: 0.25rem;
  font-weight: 600;
}

.result {
  background: #f7fff7;
  padding: 0.5rem;
  border-radius: 6px;
  border: 1px solid #e6f4ea;
}

.error-block {
  background: #fff5f5;
  padding: 0.5rem;
  border-radius: 6px;
  border: 1px solid #f3d6d6;
}

.error-text {
  white-space: pre-wrap;
  color: #b71c1c;
  margin: 0;
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 0.8rem;
}

button {
  padding: 0.45rem 0.85rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
}

.primary {
  background: #1976d2;
  color: #fff;
}

.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.neutral {
  background: #e0e0e0;
  color: #111;
}
</style>
