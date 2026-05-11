import { useState } from 'react';
import TopBar from '../../components/common/TopBar';
import Modal from '../../components/common/Modal';
import DataTable from '../../components/common/DataTable';
import { getPolicies, createPolicy, updatePolicy, deletePolicy } from '../../api/policyApi';
import { getResourceTypes } from '../../api/resourceApi';
import { useApi } from '../../hooks/useApi';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';

const DEFAULT_FORM = {
  resourceTypeId: '', maxBookingsPerUser: 4,
  holdDurationMinutes: 15, maxAdvanceBookingDays: 30, allowOverlapping: false,
};

export default function PoliciesPage() {
  const { data: policies, loading, refetch } = useApi(getPolicies);
  const { data: rtypes } = useApi(getResourceTypes);
  const [modal, setModal] = useState(null);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState(DEFAULT_FORM);

  const policyList = Array.isArray(policies) ? policies : [];
  const rtypeList = Array.isArray(rtypes) ? rtypes : [];

  const openCreate = () => { setForm(DEFAULT_FORM); setEditId(null); setModal('form'); };
  const openEdit = (p) => {
    setForm({
      resourceTypeId: p.resourceType?.id ?? '',
      maxBookingsPerUser: p.maxBookingsPerUser,
      holdDurationMinutes: p.holdDurationMinutes,
      maxAdvanceBookingDays: p.maxAdvanceBookingDays,
      allowOverlapping: p.allowOverlapping,
    });
    setEditId(p.id);
    setModal('form');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = { ...form, resourceTypeId: Number(form.resourceTypeId) };
    try {
      if (editId) { await updatePolicy(editId, payload); toast.success('Policy updated'); }
      else { await createPolicy(payload); toast.success('Policy created'); }
      setModal(null);
      refetch();
    } catch (err) { toast.error(err.message); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this policy?')) return;
    try { await deletePolicy(id); toast.success('Policy deleted'); refetch(); }
    catch (e) { toast.error(e.message); }
  };

  const columns = [
    { key: 'id', label: 'ID', width: 60, render: (v) => `#${v}` },
    { key: 'resourceType', label: 'Resource Type', render: (v) => v?.name ?? '—' },
    { key: 'maxBookingsPerUser', label: 'Max/User' },
    { key: 'holdDurationMinutes', label: 'Hold (min)' },
    { key: 'maxAdvanceBookingDays', label: 'Advance (days)' },
    { key: 'allowOverlapping', label: 'Overlapping', render: (v) => (
      <span className={`badge ${v ? 'badge-active' : 'badge-inactive'}`}>{v ? 'Yes' : 'No'}</span>
    )},
    { key: 'id', label: 'Actions', render: (id, row) => (
      <div className="flex gap-2">
        <button className="btn btn-ghost btn-icon btn-sm" onClick={() => openEdit(row)}>
          <Pencil size={13} />
        </button>
        <button className="btn btn-ghost btn-icon btn-sm" style={{ color: 'var(--color-danger)' }}
          onClick={() => handleDelete(id)}>
          <Trash2 size={13} />
        </button>
      </div>
    )},
  ];

  return (
    <div>
      <TopBar
        title="Policies"
        subtitle="Configure reservation rules per resource type"
        actions={
          <button className="btn btn-primary btn-sm" onClick={openCreate}>
            <Plus size={14} /> New Policy
          </button>
        }
      />
      <div className="app-content animate-fade-in">
        <div className="card">
          <DataTable
            columns={columns} data={policyList} loading={loading}
            emptyMessage="No policies defined yet. Create one to enforce reservation rules."
          />
        </div>
      </div>

      <Modal isOpen={modal === 'form'} onClose={() => setModal(null)}
        title={editId ? 'Edit Policy' : 'Create Policy'}>
        <form onSubmit={handleSubmit}>
          <div className="modal__body">
            <div className="form-row cols-1">
              <div className="form-group">
                <label className="form-label">Resource Type <span>*</span></label>
                <select className="form-control" value={form.resourceTypeId}
                  onChange={(e) => setForm({ ...form, resourceTypeId: e.target.value })} required>
                  <option value="">Select resource type…</option>
                  {rtypeList.map((rt) => <option key={rt.id} value={rt.id}>{rt.name}</option>)}
                </select>
              </div>
            </div>
            <div className="form-row cols-2">
              <div className="form-group">
                <label className="form-label">Max Bookings / User</label>
                <input type="number" min={1} className="form-control" value={form.maxBookingsPerUser}
                  onChange={(e) => setForm({ ...form, maxBookingsPerUser: Number(e.target.value) })} />
              </div>
              <div className="form-group">
                <label className="form-label">Hold Duration (min)</label>
                <input type="number" min={1} className="form-control" value={form.holdDurationMinutes}
                  onChange={(e) => setForm({ ...form, holdDurationMinutes: Number(e.target.value) })} />
              </div>
              <div className="form-group">
                <label className="form-label">Max Advance Days</label>
                <input type="number" min={1} className="form-control" value={form.maxAdvanceBookingDays}
                  onChange={(e) => setForm({ ...form, maxAdvanceBookingDays: Number(e.target.value) })} />
              </div>
              <div className="form-group">
                <label className="form-label">Allow Overlapping</label>
                <select className="form-control" value={form.allowOverlapping}
                  onChange={(e) => setForm({ ...form, allowOverlapping: e.target.value === 'true' })}>
                  <option value="false">No</option>
                  <option value="true">Yes</option>
                </select>
              </div>
            </div>
          </div>
          <div className="modal__footer">
            <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button type="submit" className="btn btn-primary">{editId ? 'Update' : 'Create'} Policy</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
